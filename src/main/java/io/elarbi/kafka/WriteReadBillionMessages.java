package io.elarbi.kafka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by elarbiaboussoror on 18/04/2017.
 */
public class WriteReadBillionMessages {


    static long TEN_THOUSAND = 10_000;
    static long ONE_MILLION = 1_000_000;
    static long FOUR_MILLION = 4_000_000;
    static long ONE_BILLION = 1_000_000_000;

    static long NB_MESSAGES = ONE_MILLION;

    //Run mysql before launch
    //set a Message(id: String, payload: String) table

    final static int MAX_BATCH_SIZE = 1_000;
    static int batchSize = 0;

    public static void main(String[] args) {

        String connectionURL = args[0];
        long elapsedTime = insertInBatchAndMultiThread(connectionURL, FOUR_MILLION, 128);

        System.out.println("elapsedTime in seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime));
        System.out.println("elapsedTime in minutes: " + TimeUnit.MILLISECONDS.toMinutes(elapsedTime));

    }



    private static long insertInBatchAndMultiThread(String connectionURL, long numberOfMessages, int nbThreadsPerCore) {

        long startTime = System.currentTimeMillis();
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        int nbOfParallelTasks = availableProcessors * nbThreadsPerCore;

        System.out.println("Forking " + nbOfParallelTasks + " tasks");
        ExecutorService executorService = Executors.newFixedThreadPool(nbOfParallelTasks);

        Callable<Object> callable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return insertInBatchAndSingleThread(connectionURL, numberOfMessages / nbOfParallelTasks);
            }
        };

        List<Callable<Object>> callables = new ArrayList<>();
        for (int i = 0; i < nbOfParallelTasks; i++) {
            callables.add(callable);
        }
        try {
            executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        executorService.shutdown();
        return elapsedTime;
    }

    private static long insertInBatchAndSingleThread(String connURL, long numberOfMessages) {
        long startTime = System.currentTimeMillis();
        try {

            Connection conn = DriverManager.getConnection(connURL);
            //Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/messaging?user=root&password=manager");

            String sql = "INSERT INTO MESSAGES(id, payload) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            Message message = Message.createBigMessage();

            for (long j = 0; j < numberOfMessages; j++) {

                addToBatch(message, ps);

                //store to DB every MAX_BATCH_SIZE inserts in bulk
                if (++batchSize % MAX_BATCH_SIZE == 0)
                    storeToDB(ps);

            }
            //flush remaining inserts
            storeToDB(ps);

            ps.close();
            conn.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        return elapsedTime;

    }
    private static void storeToDB(PreparedStatement ps) throws SQLException {
        ps.executeBatch();
    }

    static void addToBatch(Message message, PreparedStatement ps) throws SQLException {
        ps.setString(1, message.getId());
        ps.setString(2, message.getPayload());
        ps.addBatch();
    }
}
