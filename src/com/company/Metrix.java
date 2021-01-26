package com.company;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Metrix {
    public static String[][] getData(Object[] data){
        String [][] res = new String[data.length+1][7];
        int i=0; int sumVet =0, sumOps = 0, maxVloj = 0;
        for(Block b: (Block[])data) {
            res[i][0]=b.name;
            res[i][1]=""+b.nVet;
            res[i][2]=""+b.nVet+'/'+b.nOps;/*(b.nVet/(double)b.nOps)*/;
            res[i][3]=""+(b.vloj-1);////////////////////////////////////////////////////нужно ли???
            res[i][4]=""+(b.nVet+b.nOps+1);  //+1 - потому что от блока "начало" отходит первая дуга, +nVet - потому что от каждого ветвления отходят две дуги, а не одна. При этом ветвление - оператор
            res[i][5]=""+(b.nOps+2); //+2 - из-за блоков "начало" и "конец"
            res[i][6]=""+(b.nVet-1+2); //-1 - потому что из конца дуги не идут, +2- число компонентов связности графа
            i++;
            sumVet+=b.nVet;
            sumOps+=b.nOps;
            if(maxVloj<b.vloj) maxVloj=b.vloj;
        }
        res[i][0]="В сумме";
        res[i][1]=""+sumVet;
        res[i][2]=""+sumVet+'/'+sumOps/*(sumVet/(double)sumOps)*/;
        res[i][3]=""+(maxVloj-1);///////////////////////////////////////////////////////нужно ли???
        res[i][4]=""+'-'/*(sumVet+sumOps+1)*/;
        res[i][5]=""+'-'/*(sumOps+2)*/;    ////////////////////////////////////////имеет ли смысл метрика Маккейба для нескольких графов, некоторые из которых - не связаны друг с другом???
        res[i][6]=""+'-'/*(sumVet+1)*/;
        return res;
    }
    static Block funcs;
    public static String readCode(String adress) throws IOException {
        File cod = new File(adress);
        if (!cod.exists())
            throw new IOException();
        FileReader rdr = new FileReader(cod);
        int c;
        String code = "";
        while ((c = rdr.read()) != -1) {
            code += (char) c;
        }
        return code;
    }

    public static Object[] calculate(String code) throws RuntimeException {
        code = clean(code);
        code = replaceOperators(code);
        code=getMethodsAndReplace(code);
        int size=0;
        for(Block curr = funcs;curr!=null;curr=curr.next) {
            curr=processFunction(curr, code.substring(curr.start+1, curr.end-1));
            size++;
        }
        Object[] res = new Block[size]; int i=0;
        for(Block curr=funcs;curr!=null;curr=curr.next) {
            res[i]=curr;
            i++;
        }
        return res;
    }

    public static String clean(String code) {
        String res = code;
        Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
        Matcher matcher = pattern.matcher(code);
        String cd = code;
        while(matcher.find()) {
            cd=cd.concat(code.substring(matcher.start(),matcher.end()));
        }
        code = cd;

        pattern = Pattern.compile("\r");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");

        pattern = Pattern.compile("\t");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll(" ");

        pattern = Pattern.compile("[/]{2}.+?\n");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll(" ");

        pattern = Pattern.compile("import.+?\n");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");

        pattern = Pattern.compile("\".+?\"");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");
        pattern = Pattern.compile("'.+?'");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");

        pattern = Pattern.compile("//.+?$");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");

        pattern = Pattern.compile("\\Wnew\\W");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll(" ");

        pattern = Pattern.compile("/[*].*?\n*.*?\n*.*?\n*.*?\n*.*?\n*.*?[*]/");
        matcher = pattern.matcher(code);
        code = matcher.replaceAll("");

        return code;
    }
    public static String getMethodsAndReplace(String code) {
        Pattern pattern = Pattern.compile("\\b +?\\w+?\\(");
        Matcher matcher = pattern.matcher(code);
        funcs = null; int start, end;
        if(matcher.find()&&(funcs == null)) {
            funcs = new Block(code.substring(matcher.start()+1,matcher.end()-1).trim());
        }
        Block curr = funcs;
        while(matcher.find()) {
            curr.next=new Block(code.substring(matcher.start()+1,matcher.end()-1).trim());
            curr=curr.next;
        }
        curr=funcs;
        code=replaceMethods(code);
        matcher = pattern.matcher(code);
        for(int i=0;matcher.find();i++) {
            String temp = code.substring(matcher.start());
            start = temp.indexOf('{');
            end = findBlockEnd(temp.substring(start+1),0);
            start+=matcher.start();
            end+=start+1;
            curr.end=end;
            curr.start=start;
            curr=curr.next;
        }
        return code;
    }

    public static String replaceMethods(String code) {
        String res = code;
        Block curr = funcs;
        for(int index=0;curr!=null;index++) {
            Pattern pattern = Pattern.compile("\\."+curr.name+"[ \n]*?\\(");
            Matcher matcher = pattern.matcher(res);
            res = matcher.replaceAll("^"+index);
            curr=curr.next;
        }
        return res;
    }
    public static String replaceOperators(String code) throws RuntimeException{
        String res = code;
        File src = new File("Keys.txt");

        if (!src.exists())
            throw new RuntimeException();
        try {
            FileReader rdr = new FileReader(src);
            int c;
            String[] characteristics = { "", "" };
            int index = 0;
            while ((c = rdr.read()) != -1) {
                switch ((char) c) {
                    case '$':
                        index++;
                        break;
                    case '\r':
                        break;
                    case '\n':
                        index--;
                        Pattern pattern = Pattern.compile(characteristics[1]);
                        Matcher matcher = pattern.matcher(res);
                        res = matcher.replaceAll(characteristics[0]);
                        characteristics[0] = "";
                        characteristics[1] = "";
                        break;
                    default:
                        characteristics[index] += (char) c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return res;
    }

    private static int findBlockEnd(String code, int vloj) {//для блочной обработки
        Pattern op = Pattern.compile("[{]");
        Matcher o = op.matcher(code);
        Pattern cl = Pattern.compile("[}]");
        Matcher c = cl.matcher(code);
        if(o.find()) {
            int star = o.start(); int en=code.length();
            if(c.find()) en=c.start();
            else throw new RuntimeException("Invalid breakets!");
            if(star<en) {
                return findBlockEnd(code.substring(star+1),vloj+1)+star+1;
            } else {
                if(vloj>0) {
                    return findBlockEnd(code.substring(en+1), vloj-1)+en+1;
                } else {
                    return en;
                }
            }
        }else {
            if(c.find()) {
                int en = c.start();
                if(vloj==0) return en;
                else {
                    return findBlockEnd(code.substring(en+1), vloj-1)+en+1;
                }
            }
            else throw new RuntimeException("Invalid breakets!");
        }
    }

    private static int findCaseEnd(String code) { //для блочной обработки
        Pattern cas = Pattern.compile("#7");
        Matcher mcas = cas.matcher(code);
        Pattern def = Pattern.compile("#8");
        Matcher mdef = def.matcher(code);
        int de = code.length(); int ca = code.length();
        if(mdef.find()) {
            de = mdef.start();
            if(mcas.find()) {
                ca = mcas.start();
            }
            if(ca<de) return ca-2;
            else return de-2;
        } else if (mcas.find()) {
            ca = mcas.start();
            return ca-2;
        } else throw new RuntimeException("Invalid switch-case operator!");
    }

    private static int gainFuncNumber(String code, int start) { //Использует особенности препроцессорного синтаксиса для получения номера функции
        int res = 0; int i=start;
        while(code.charAt(i)>47&&code.charAt(i)<58) {
            res*=10;
            res+=code.charAt(i)-'0';
            i++;
        }
        return res;
    }
    private static int findOperators(String subCode) {  //количество операторов равно количеству точек с запятой + количество ветвлений
        int res=0;
        Pattern pattern = Pattern.compile(";");
        Matcher matcher = pattern.matcher(subCode);
        while(matcher.find()) {
            res++;
        }
        return res;
    }

    private static Block processFunction(Block func, String code) { //////////Главный обработчик (логика)
        Block res = func; int nIf=0, nDo=0, curVloj = 0, pos=0;
        res.nOps+=findOperators(code);
        operators = new Stack<Integer>();
        Pattern pattern = Pattern.compile("[#^][0-9]+?");
        Matcher matcher = pattern.matcher(code);
        if(matcher.find()) {
            pos=matcher.start();
            switch(code.charAt(pos)) {
                case '#': //////////обработка ветвлений
                    int opNumber=gainFuncNumber(code,pos+1);
                    switch(opNumber) {
                        case 1:   //for
                            res.nVet++;
                            res.nOps++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(1);
                            break;
                        case 2: //do
                            nDo++;            //рaстёт вложенность do-while
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(2);
                            break;
                        case 3:  //while
                            res.nVet++;
                            res.nOps++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(3);
                            break;
                        case 4:  //if
                            res.nVet++;
                            nIf++;         //рaстёт вложенность if-else
                            res.nOps++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(4);
                            break;
                        case 6:  //switch - вроде как есть, а вроде как и нет)))
                            break;
                        default:
                            throw new RuntimeException("Этого не должно было произойти!!!");
                    }
                    break;

                case '^': ///////////////обработка подпрограмм
                    int funcNumber=gainFuncNumber(code,pos+1);
                    Block curr=funcs;
                    for(int i=0;i<funcNumber;i++) {
                        curr=curr.next;
                    }
                    res.nVet+=curr.nVet;
                    res.nOps+=curr.nOps;
                    res.nOps--;
                    if(curVloj+curr.vloj>res.vloj) res.vloj=curVloj+curr.vloj;
                    break;
                default:
                    throw new RuntimeException("Этого не должно было произойти...");
            }

            res=decide(res,code,nIf,nDo,curVloj,pos,0);
        }
        return res;
    }

    private static Block decide(Block res, String code, int nIf, int nDo, int curVloj, int pos, int pushed) { //вспомогательный метод для разгрузки главного обработчика
        int breaketIndex = code.indexOf('{',pos+2);
        if(breaketIndex==-1) breaketIndex=code.length()+1;

        int dotComIndex = code.indexOf(';',pos+2);
        if(dotComIndex==-1) dotComIndex=code.length()+1;

        int nextExprIndex = code.indexOf('#',pos+2);
        if(nextExprIndex==-1) nextExprIndex=code.length();

        if(nextExprIndex>code.indexOf('^',pos+2)&&(code.indexOf('^',pos+2)!=-1)) {
            nextExprIndex=code.indexOf('^',pos+2);
        }

        if(nextExprIndex<breaketIndex&&nextExprIndex<dotComIndex) {
            res=process(res,code.substring(nextExprIndex-1),nIf,nDo,curVloj,false,pushed);//обработка след. оператора без снижения уровня вложености
        } else if(dotComIndex<breaketIndex) {
            res=process(res,code.substring(dotComIndex+1),nIf,nDo,curVloj,true,pushed);//обработка след. оператора и снижение уровня вложенности
        } else {
            int endBreaket = findBlockEnd(code.substring(breaketIndex+1),0);
            endBreaket+=breaketIndex+1;
            res=process(res,code.substring(breaketIndex+1, endBreaket-1),0,0,curVloj,false,0);    //обработка блока кода
            res=process(res,code.substring(endBreaket+1),nIf,nDo,curVloj,true,pushed);                //последующая обработка оставшегося кода
        }
        return res;
    }

    private static Stack<Integer> operators; //для стековой обработки

    private static Block process(Block func, String subCode, int nIf, int nDo, int curVloj, boolean dotCom, int pushed) {//рекурсивная часть главного обработчика (рекурсивная логика)
        Block res = func; int pos=0;
        if(dotCom) {
            while((!operators.isEmpty())&&(pushed>0)&&operators.peek()!=2&&operators.peek()!=4) { //стек не пустой и на его вершине не то, что "требует закрытия"
                operators.pop(); pushed--;
                curVloj--;
            }
        }
        Pattern pattern = Pattern.compile("[#^][0-9]+?");
        Matcher matcher = pattern.matcher(subCode);
        if(matcher.find()) {
            pos=matcher.start();
            switch(subCode.charAt(pos)) {
                case '#':        //////////обработка ветвлений
                    int opNumber=gainFuncNumber(subCode,pos+1);
                    switch(opNumber) {
                        case 1:   //for
                            res.nOps++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(1); pushed++;
                            res.nVet++;
                            break;
                        case 2: //do
                            nDo++;            //рaстёт вложенность do-while
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(2); pushed++;
                            break;
                        case 3:  //while
                            if(nDo==0||!dotCom) {
                                res.nOps++;
                                curVloj++;
                                if(curVloj>res.vloj) res.vloj=curVloj;
                                operators.push(3); pushed++;
                            } else {
                                while(operators.peek()!=2) {  //исключаем if-ы, пока не доберёмся до do, который закроем while-ом
                                    if(operators.peek()==4) nIf--;
                                    operators.pop(); pushed--;
                                    curVloj--;
                                }
                                operators.pop(); pushed--;
                                nDo--; curVloj--;
                            }
                            res.nVet++;
                            break;
                        case 4:  //if
                            nIf++;         //рaстёт вложенность if-else
                            res.nOps++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            operators.push(4); pushed++;
                            res.nVet++;
                            break;
                        case 5:  //else
                            operators.pop(); operators.push(5);
                            nIf--;
                            break;
                        case 6:  //switch - вроде как есть, а вроде как и нет)))
                            break;
                        case 7:  //case
                            res.nOps++;
                            res.nVet++;
                            curVloj++;
                            if(curVloj>res.vloj) res.vloj=curVloj;
                            int endOfCase = findCaseEnd(subCode.substring(pos+2));
                            res=process(res,subCode.substring(pos+2,endOfCase),0,0,curVloj,false,0);    //обработка блока кода
                            break;
                        case 8:  //default
                            ////////////////////////////////////////////////////////////////////////аналог else, так что вложенность не растёт!!!
                            break;
                        default:
                            throw new RuntimeException("Этого не должно было произойти!!!");
                    }
                    break;
                case '^':   ///////////////обработка подпрограмм
                    int funcNumber=gainFuncNumber(subCode,pos+1);
                    Block curr=funcs;
                    for(int i=0;i<funcNumber;i++) {
                        curr=curr.next;
                    }
                    res.nVet+=curr.nVet;
                    res.nOps+=curr.nOps;
                    res.nOps--;
                    if(curVloj+curr.vloj>res.vloj) res.vloj=curVloj+curr.vloj;
                    break;
                default:
                    throw new RuntimeException("Этого не должно было произойти...");
            }
            res=decide(res,subCode,nIf,nDo,curVloj,pos,pushed);//выбор пути...))))
        } else {
            while(pushed>0) {
                operators.pop(); pushed--;
                curVloj--;
            }
        }
        return res;
    }

    private static class Block {
        String name; int start,end;
        int nOps, nVet, vloj;
        public Block(String name) {
            this.name = name;
            nOps=nVet=vloj=0;
        }
        Block next;
    }
}