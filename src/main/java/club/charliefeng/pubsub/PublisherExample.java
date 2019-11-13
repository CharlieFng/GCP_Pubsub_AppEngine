package club.charliefeng.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PublisherExample {

    private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

    /** Publish messages to a topic.
     * @param args topic name, number of messages
     */
    public static void main(String... args) throws Exception {


        String apiKey = "KHWA9XS16KXGID40";
        int timeout = 3000;
        AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
        TimeSeries stockTimeSeries = new TimeSeries(apiConnector);

        List<StockData> stockData = null;
        try {
            IntraDay response = stockTimeSeries.intraDay("MSFT", Interval.ONE_MIN, OutputSize.COMPACT);
            Map<String, String> metaData = response.getMetaData();
//            System.out.println("Information: " + metaData.get("1. Information"));
//            System.out.println("Stock: " + metaData.get("2. Symbol"));

            stockData = response.getStockData();
//            stockData.forEach(stock -> {
//                System.out.println("date:   " + stock.getDateTime());
//                System.out.println("open:   " + stock.getOpen());
//                System.out.println("high:   " + stock.getHigh());
//                System.out.println("low:    " + stock.getLow());
//                System.out.println("close:  " + stock.getClose());
//                System.out.println("volume: " + stock.getVolume());
//            });
        } catch (AlphaVantageException e) {
            System.out.println("something went wrong");
        }


        if(stockData == null) return;

        String topicId = "stock-realtime";
        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
        Publisher publisher = null;
        List<ApiFuture<String>> futures = new ArrayList<>();
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        try {
            publisher = Publisher.newBuilder(topicName).build();

            for (StockData item: stockData) {
                String str = objMapper.writeValueAsString(item);
                System.out.println(str);
                ByteString data = ByteString.copyFromUtf8(str);
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                        .setData(data)
                        .build();

                ApiFuture<String> future = publisher.publish(pubsubMessage);
                futures.add(future);
            }
        } finally {
            List<String> messageIds = ApiFutures.allAsList(futures).get();

            for (String messageId : messageIds) {
                System.out.println(messageId);
            }

            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
