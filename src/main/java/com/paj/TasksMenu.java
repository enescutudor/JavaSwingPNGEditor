package com.paj;

import com.sun.source.util.TaskEvent;
import org.reflections.Reflections;

import javax.naming.LinkLoopException;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.event.TableColumnModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;

public class TasksMenu {

    private JPanel mainPanel;
    private JButton addButton;
    private JButton deleteButton;
    private JPanel buttonLayout;
    private JPanel tasksLayout;
    private JButton processButton;
    private JScrollPane tasksScroller;
    private JTable tasksTable;
    private DefaultTableModel tasksTableModel;
    public static List<Task> tasks = new LinkedList<>();
    public static ThreadPoolExecutor workQueue = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    public static final Object tableLock = new Object();
    public static CyclicBarrier tasksProcessingBarrier = null;

    public TasksMenu() {

        tasksTableModel = new DefaultTableModel(null, new String[]{"Thumbnail", "Path", "Completed"}) {
            @Override
            public Class getColumnClass(int column) {
                return (column == 0) ? Icon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        tasksTable.setModel(tasksTableModel);
        tasksTable.setRowHeight(100);
        tasksTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        tasksTable.getColumnModel().getColumn(1).setPreferredWidth(720);
        tasksTable.getColumnModel().getColumn(2).setPreferredWidth(380);
        tasksTable.getColumnModel().getColumn(0).setResizable(false);
        tasksTable.getColumnModel().getColumn(1).setResizable(false);
        tasksTable.getColumnModel().getColumn(2).setResizable(false);
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "png");
                fileChooser.setFileFilter(filter);
                int option = fileChooser.showOpenDialog(mainPanel);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (tasks.stream().noneMatch(t -> t.getPathToFile().equals(file.getAbsolutePath()))) {
                        Task t = new Task(file.getAbsolutePath());
                        ImageIcon thumbnail = new ImageIcon(file.getAbsolutePath());
                        Image aux = thumbnail.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                        thumbnail.setImage(aux);
                        tasks.add(t);
                        Object[] newRow = new Object[]{thumbnail, file.getAbsolutePath(), "No"};
                        tasksTableModel.addRow(newRow);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel,
                                "The file has already been selected for a task",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<String> pathsToBeDeleted = Arrays.stream(tasksTable.getSelectedRows())
                        .mapToObj(i -> tasksTable.getValueAt(i, 1)).map(Object::toString).collect(Collectors.toList());
                int[] rowsToBeDeleted = tasksTable.getSelectedRows();
                Arrays.sort(rowsToBeDeleted);

                for (int i = rowsToBeDeleted.length - 1; i >= 0; i--) {
                    tasksTableModel.removeRow(rowsToBeDeleted[i]);
                }
                tasks = tasks.stream().filter(t -> !pathsToBeDeleted.contains(t.getPathToFile())).collect(Collectors.toCollection(LinkedList::new));
            }
        });

        tasksTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {

                    List<String> paths = tasks.stream()
                            .map(Task::getPathToFile)
                            .filter(pathToFile -> pathToFile.equals(tasksTableModel.getValueAt(table.getSelectedRow(), 1)))
                            .collect(Collectors.toList());
                    String path = paths.get(0);
                    JFrame frame = new JFrame("Jobs");
                    for (Task t : tasks) {
                        if (t.getPathToFile().equals(path)) {
                            frame.setContentPane(new JobsMenu(t).mainJobsPanel);
                        }
                    }

                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.pack();
                    frame.setSize(new Dimension(900, 500));
                    frame.setResizable(false);
                    frame.setVisible(true);
                }
            }
        });


        processButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] selectedRows = tasksTable.getSelectedRows();
                HashMap<Integer, Task> rowTask = new HashMap<>();
                for (int sr : selectedRows) {
                    rowTask.put(sr,
                        tasks.stream()
                            .filter(t -> t.getPathToFile().equals(tasksTableModel.getValueAt(sr, 1)))
                            .collect(Collectors.toList()).get(0));
                }


                int numberOfBarriers = (int)rowTask.values().stream().filter(t -> !t.isCompleted()).count();
                tasksTable.setEnabled(false);
                processButton.setEnabled(false);
                addButton.setEnabled(false);
                deleteButton.setEnabled(false);
                TasksMenu.tasksProcessingBarrier = new CyclicBarrier(numberOfBarriers + 1);
                rowTask.forEach((key, value) -> workQueue.execute(() -> {
//                    if (value.isCompleted())
//                        return;
                    int[][] currentPicture = null;
                    try {
                        currentPicture = TasksMenu.getImageFromFile(value.getPathToFile());
                    } catch (IOException ioException) {
                        synchronized (tableLock) {
                            tasksTableModel
                                    .setValueAt("No (" + ioException.getMessage() + ")", key, 2 );
                        }
                        try {
                            TasksMenu.tasksProcessingBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        return;
                    }
                    for (Job j : value.getJobList()) {
                        try {
                            currentPicture = j.executeProcessingJob(currentPicture);
                        } catch (Exception e1) {
                            synchronized (tableLock) {
                                tasksTableModel
                                        .setValueAt("No (" + e1.getMessage() + ")", key, 2 );
                            }
                            try {
                                TasksMenu.tasksProcessingBarrier.await();
                            } catch (InterruptedException | BrokenBarrierException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                            return;
                        }
                    }
                    synchronized (tableLock) {
                        tasksTableModel
                                .setValueAt("Yes (" + LocalTime.now().toString() + ")", key, 2 );
                    }
                    value.setCompleted(true);
                    try {
                        TasksMenu.tasksProcessingBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }));
                try {
                    TasksMenu.tasksProcessingBarrier.await();
                } catch (InterruptedException | BrokenBarrierException interruptedException) {
                    interruptedException.printStackTrace();
                }
                tasksTable.setEnabled(true);
                processButton.setEnabled(true);
                addButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        });
    }

    public static int[][] getImageFromFile(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        int height = image.getHeight();
        int width = image.getWidth();

        int[][] picture = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                picture[i][j] = image.getRGB(j, i);
            }
        }
        return picture;
    }

    public static void writeImageToFile(String path, int[][] picture) throws IOException {
        int height = picture.length;
        int width = picture[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.setRGB(j, i, picture[i][j]);
            }
        }

        File output = new File(path);
        ImageIO.write(result, "png", output);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tasks");
        frame.setContentPane(new TasksMenu().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(1200, 500));
        frame.setResizable(false);
        frame.setVisible(true);

        try {
            int[][] imageData = getImageFromFile("src/main/test.png");
            writeImageToFile("src/main/out.png", new TrimJob(50, 10).executeProcessingJob(imageData));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
