package com.company;

import com.company.Metrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

class Interface {
    public static void create() {
        JFrame frame = new JFrame("Метрики Джилба и Маккейба для Scala");
        frame.setSize(new Dimension(530, 120));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextField adr = new JTextField("", 25);
        JLabel ad = new JLabel("Введите адрес файла с кодом:");
        JButton cal = new JButton("Вычислить метрики Джилба и Маккейба");

        cal.addActionListener(new ActionListener() {// вычисление метрики
            public void actionPerformed(ActionEvent ever) {
                if (adr.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, " Вы не ввели адрес!", "Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        drawTable(Metrix.calculate(Metrix.readCode(adr.getText())));
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Считывание из файла невозможно!", "Warning",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (RuntimeException e) {
                        JOptionPane.showMessageDialog(null, "Ошибки в коде!", "Warning", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }
        });

        JLayeredPane lp = frame.getLayeredPane();// послойное расположение компонентов окна
        ad.setBounds(10, 10, 190, 20);
        adr.setBounds(200, 10, 300, 20);
        cal.setBounds(100, 40, 300, 20);
        lp.add(ad, JLayeredPane.POPUP_LAYER);
        lp.add(adr, JLayeredPane.POPUP_LAYER);
        lp.add(cal, JLayeredPane.POPUP_LAYER);

        frame.setVisible(true);
    }

    public static void drawTable(Object[] metrix) {
        JFrame frame = new JFrame("Метрики Джилба и Маккейба");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columnNames = { //Заголовки столбцов
                "Название метода", "Абсолютная сложность по Джилбу", "Относительная сложность по Джилбу", "Максимальная вложенность", "Количество дуг графа", "Количество вершин графа", "Метрика Маккейба" };//TODO

        String[][] data = Metrix.getData(metrix); //Собственно таблица

        JTable table = new JTable(data, columnNames);

        JScrollPane scrollPane = new JScrollPane(table);

        frame.getContentPane().add(scrollPane);
        frame.setPreferredSize(new Dimension(950, 500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}