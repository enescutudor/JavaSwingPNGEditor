# JavaSwingPNGEditor

## Componente grafice

### Ecranul de task-uri

Ecranul principal al aplicatiei este reprezentat de o lista de task-uri, reprezentate de o imagine, in format **.png**, de pe sistemul de fisiere local.
<br>
Adaugarea unei imagini se face cu butonul Add, stergerea se face cu butonul Delete (suporta selectie multipla), iar procesarea task-urilor se face cu butonul Process (de asemenea, suporta selectie multipla).
<br>
Un task, la nivel programatic, este reprezentat de o cale in sistemul de fisiere catre o imagine, si o lista de job-uri, care reprezinta diferite procesari ale imaginii.
<br>
Actionand dublu-click peste una din liniile tabelului va rezulta in deschiderea unui ecran de modificare a listei de job-uri asociate task-ului respectiv.
<br>
Procesarea task-urilor selectate se face in paralel cu ajutorul unei cozi de lucru de tipul ThreadPoolExecutor, in care, pentru fiecare task, se genereaza cate un thread care inlantuieste job-urile asociate, iar, la final, modifica coloana Completed fie cu momentul finalizarii, fie cu un mesaj de eroare, daca este cazul.
<br>
```
TasksMenu.tasksProcessingBarrier = new CyclicBarrier(numberOfBarriers + 1);
rowTask.forEach((key, value) -> workQueue.execute(() -> {
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
```
Nu se pot adauga 2 task-uri care au aceeasi cale.
### Ecranul de job-uri

Actionand dublu-click peste una din liniile tabelului va rezulta in deschiderea ecranului de modificare a listei de job-uri.
<br>
Butoanele Add si Delete sunt si aici prezente, cu acelasi rol, avand deosebirea ca nu se pot selecta mai multe job-uri pentru stergere.
<br>
Se poate schimba ordinea de executie a job-urilor folosind butoanele inscriptionate cu sageti.
<br>
La nivel programatic, un job este o instanta a unei clase derivate din clasa abstracta Job, care defineste metoda:
<br>
```public abstract int[][] executeProcessingJob(int[][] picture) throws Exception```
<br>
si 2 utilitare de procesare a unui pixel:
<br>
```int[] getRGBAFromPixel(int pixelColorValue)```
<br>
```int getColorIntValFromRGBA(int[] colorData)```
<br>
Tipurile de joburi implementate pentru acest proiect sunt urmatoarele:
- ```DrawCircleJob``` care faciliteaza desenarea unui cerc pe imagine
- ```DrawRectangleJob``` care faciliteaza desenarea unui dreptunghi pe imagine
- ```MirrorHorizontalJob``` si ```MirrorVerticalJob``` cu ajutorul carora se poate oglindi imaginea
- ```NegativeJob``` cu ajutorul careia se pot inversa culorile imganinii
- ```TrimJob``` care faciliteaza decuparea marginilor imaginii
- ```SaveJob``` care faciliteaza salvarea imaginii
<br>
Selectarea tipului de job se face dintr-un meniu de tip dropdown, urmat de un formular de configurare a job-ului, acolo unde este cazul.
<br>
Afisarea job-urilor create, cu tot cu parametrii lor, in momentul deschiderii unui task, se face cu ajutorul Reflection API.
## Alte precizari
Codul are drept target framework Java SDK 15.
Interfata grafica a fost creata folosind designer-ul grafic disponibil in IntelliJ IDEA si Java Swing.
