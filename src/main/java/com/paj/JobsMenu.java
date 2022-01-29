package com.paj;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Vector;

public class JobsMenu {
    public JPanel mainJobsPanel;
    private JButton addButton;
    private JPanel buttonLayout;
    private JPanel jobsLayout;
    private JButton deleteButton;
    private JScrollPane jobsScroll;
    private JTable jobsTable;
    private JButton upButton;
    private JButton downButton;
    private DefaultTableModel jobsTableModel;
    private Task task;


    public JobsMenu(Task task) {
        this.task = task;
        jobsTableModel = new DefaultTableModel(null, new String[]{"Job", "Parameter"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        upButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = jobsTable.getSelectedRow();
                if (selectedRow > 0) {
                    jobsTableModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
                    Job aux = task.getJobList().get(selectedRow);
                    task.getJobList().set(selectedRow, task.getJobList().get(selectedRow - 1));
                    task.getJobList().set(selectedRow - 1, aux);
                    jobsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                }
            }
        });
        downButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = jobsTable.getSelectedRow();
                if (selectedRow < jobsTable.getRowCount() - 1) {
                    jobsTableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
                    Job aux = task.getJobList().get(selectedRow);
                    task.getJobList().set(selectedRow, task.getJobList().get(selectedRow + 1));
                    task.getJobList().set(selectedRow + 1, aux);
                    jobsTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
                }
            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = jobsTable.getSelectedRow();
                if (selectedRow != -1) {
                    jobsTableModel.removeRow(selectedRow);
                    task.getJobList().remove(selectedRow);
                }
            }
        });

        jobsTable.setModel(jobsTableModel);
        jobsTable.setRowHeight(50);
        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(800);
        jobsTable.getColumnModel().getColumn(0).setResizable(false);
        jobsTable.getColumnModel().getColumn(1).setResizable(false);

        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jobsTable.setDragEnabled(true);
        jobsTable.setDropMode(DropMode.INSERT_ROWS);


        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String chosenJob = (String) JOptionPane.showInputDialog(
                        null,
                        "What kind of job do you want to add?",
                        "Choose job",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {"Save", "Trim", "Negative", "Mirror Horizontally", "Mirror Vertically", "Draw Circle", "Draw Rectangle", "Greyscale"},
                        "Trim");
                switch (chosenJob) {
                    case "Save":
                        generateSaveForm();
                        break;
                    case "Trim":
                        generateTrimForm();
                        break;
                    case "Negative":
                        generateNegativeJob();
                        break;
                    case "Mirror Horizontally":
                        generateMirrorHorizontalJob();
                        break;
                    case "Mirror Vertically":
                        generateMirrorVerticalJob();
                        break;
                    case "Draw Circle":
                        generateDrawCircleForm();
                        break;
                    case "Greyscale":
                        generateGreyScaleJob();
                        break;
                    case "Draw Rectangle":
                        generateDrawRectangleForm();
                        break;
                }
            }
        });
        for (Job j : task.getJobList()) {
            Object[] rowContent = new Object[2];
            try {
                rowContent[0] = j.getClass().getField("Name").get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            rowContent[1] = "";
            Field[] fields = j.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                try {
                    if (!f.getName().equals("Name"))
                        rowContent[1] += f.getName() + ": " + f.get(j) + "    ";
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    f.setAccessible(false);
                }
            }
            jobsTableModel.addRow(rowContent);
        }
    }


    public void generateSaveForm() {
        SaveJobForm sjf = new SaveJobForm();
        JFrame frame = new JFrame("Job creation");
        frame.setContentPane(sjf.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(300, 300));
        frame.setResizable(false);
        frame.setVisible(true);
        sjf.saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Paths.get(sjf.path.getText());
                } catch (InvalidPathException | NullPointerException ex) {
                    JOptionPane.showMessageDialog(sjf.mainPanel,
                            "Invalid path",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (Task t : TasksMenu.tasks) {
                    for (Job j : t.getJobList()) {
                        if (j instanceof SaveJob) {
                            if (((SaveJob)j)
                                    .getPath()
                                    .toUpperCase(Locale.ROOT)
                                    .equals(sjf.path
                                            .getText()
                                            .toUpperCase(Locale.ROOT))) {
                                JOptionPane.showMessageDialog(sjf.mainPanel,
                                        "This file has already been selected for another save",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }
                task.getJobList().add(new SaveJob(sjf.path.getText()));
                Object[] rowContent = new Object[2];
                rowContent[0] = "Save";
                rowContent[1] = "path: " + sjf.path.getText();
                jobsTableModel.addRow(rowContent);
                frame.dispose();
            }
        });

    }

    public void generateTrimForm() {
        TrimJobForm tjf = new TrimJobForm();
        JFrame frame = new JFrame("Job creation");
        frame.setContentPane(tjf.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(300, 300));
        frame.setResizable(false);
        frame.setVisible(true);
        tjf.saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int totalTrimmedWidth = 0;
                int totalTrimmedHeight = 0;
                try {
                    totalTrimmedWidth = 2 * Integer.parseInt(tjf.width.getText());
                    totalTrimmedHeight = 2 * Integer.parseInt(tjf.height.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(tjf.mainPanel,
                            "Incorect format given for the parameters",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (Job j : task.getJobList()) {
                    if (j instanceof TrimJob) {
                        TrimJob tj = (TrimJob) j;
                        totalTrimmedWidth += 2 * tj.getWidth();
                        totalTrimmedHeight += 2 * tj.getHeight();
                    }
                }

                int pictureWidth, pictureHeight;
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new File(task.getPathToFile()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                pictureHeight = image.getHeight();
                pictureWidth = image.getWidth();

                if (totalTrimmedWidth >= pictureWidth || totalTrimmedHeight >= pictureHeight) {
                    JOptionPane.showMessageDialog(tjf.mainPanel,
                            "Trim exceeds picture size",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                task.getJobList().add(new TrimJob(Integer.parseInt(tjf.width.getText()), Integer.parseInt(tjf.height.getText())));
                Object[] rowContent = new Object[2];
                rowContent[0] = "Trim";
                rowContent[1] = "width: " + Integer.parseInt(tjf.width.getText()) + "    " + "height: " + Integer.parseInt(tjf.height.getText());
                jobsTableModel.addRow(rowContent);
                frame.dispose();
            }
        });

    }
    public void generateNegativeJob() {
        task.getJobList().add(new NegativeJob());
        Object[] rowContent = new Object[2];
        rowContent[0] = "Negative";
        rowContent[1] = "";
        jobsTableModel.addRow(rowContent);
    }

    public void generateMirrorHorizontalJob() {
        task.getJobList().add(new MirrorHorizontalJob());
        Object[] rowContent = new Object[2];
        rowContent[0] = "Mirror Horizontally";
        rowContent[1] = "";
        jobsTableModel.addRow(rowContent);
    }
    public void generateMirrorVerticalJob() {
        task.getJobList().add(new MirrorVerticalJob());
        Object[] rowContent = new Object[2];
        rowContent[0] = "Mirror Vertically";
        rowContent[1] = "";
        jobsTableModel.addRow(rowContent);
    }
    public void generateDrawCircleForm() {
        DrawCircleJobForm dcjf = new DrawCircleJobForm();
        JFrame frame = new JFrame("Job creation");
        frame.setContentPane(dcjf.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(800, 300));
        frame.setResizable(false);
        frame.setVisible(true);
        dcjf.saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x, y, radius, R, G, B, A;
                try {
                    x = Integer.parseInt(dcjf.x.getText());
                    y = Integer.parseInt(dcjf.y.getText());
                    radius = Integer.parseInt(dcjf.radius.getText());
                    R = Integer.parseInt(dcjf.R.getText());
                    G = Integer.parseInt(dcjf.G.getText());
                    B = Integer.parseInt(dcjf.B.getText());
                    A = Integer.parseInt(dcjf.A.getText());
                    if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255 || A < 0 || A > 255) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dcjf.mainPanel,
                            "Parameters format not correct",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                DrawCircleJob dcw = new DrawCircleJob(x, y, radius, R, G, B, A);
                task.getJobList().add(dcw);
                Object[] rowContent = new Object[2];
                rowContent[0] = "Draw Circle";
                rowContent[1] = "x: " + x + "    " +
                        "y: " + y + "    " +
                        "radius: " + radius + "    " +
                        "R: " + R + "    " +
                        "G: " + G + "    " +
                        "B: " + B + "    " +
                        "A: " + A + "    ";
                jobsTableModel.addRow(rowContent);
                frame.dispose();
            }
        });

    }

    public void generateGreyScaleJob() {
        task.getJobList().add(new GreyscaleJob());
        Object[] rowContent = new Object[2];
        rowContent[0] = "Greyscale";
        rowContent[1] = "";
        jobsTableModel.addRow(rowContent);
    }

    public void generateDrawRectangleForm() {
        DrawRectangleJobForm drjf = new DrawRectangleJobForm();
        JFrame frame = new JFrame("Job creation");
        frame.setContentPane(drjf.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(800, 300));
        frame.setResizable(false);
        frame.setVisible(true);
        drjf.saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x, y, width, height, R, G, B, A;
                try {
                    x = Integer.parseInt(drjf.x.getText());
                    y = Integer.parseInt(drjf.y.getText());
                    width = Integer.parseInt(drjf.width.getText());
                    height = Integer.parseInt(drjf.height.getText());
                    R = Integer.parseInt(drjf.R.getText());
                    G = Integer.parseInt(drjf.G.getText());
                    B = Integer.parseInt(drjf.B.getText());
                    A = Integer.parseInt(drjf.A.getText());
                    if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255 || A < 0 || A > 255) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(drjf.mainPanel,
                            "Parameters format not correct",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                DrawRectangleJob dcw = new DrawRectangleJob(x, y, width, height, R, G, B, A);
                task.getJobList().add(dcw);
                Object[] rowContent = new Object[2];
                rowContent[0] = "Draw Circle";
                rowContent[1] = "x: " + x + "    " +
                        "y: " + y + "    " +
                        "width: " + width + "    " +
                        "height: " + height + "    " +
                        "R: " + R + "    " +
                        "G: " + G + "    " +
                        "B: " + B + "    " +
                        "A: " + A + "    ";
                jobsTableModel.addRow(rowContent);
                frame.dispose();
            }
        });

    }
}
