import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.time.Instant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MockData {

    static Gson gson = new Gson();
    public static List<Integer> getUserTransactions(int uId, String txnType , String monthYear){

        // first commadn
        String baseUrl = "https://jsonmock.hackerrank.com/api/transactions/search?userId="+uId;

        JsonObject response ;
        int totalPages = 0;

        int pages = 0;

        int[] debits ={0};

        double[] totalAmount ={0} ;

        List<Record> recordList = new ArrayList<>();

        List<Integer> data = new ArrayList<>();
        try {
            response = makeRequest(baseUrl);

            totalPages = response.get("total_pages").getAsInt();

            if (totalPages == 0){
                // no records found
                data.add(-1);

                return data;
            }

            pages = response.get("page").getAsInt();

            // fetch for page 1

            process(txnType , monthYear ,  response.get("data").getAsJsonArray() , recordList , totalAmount , debits);

        }catch (Exception e){
            data.add(-1);
            return data;
        }

        pages++; // move to next page

        System.out.println();
        while (pages <= totalPages){

            String url = baseUrl +"&page="+pages;

            System.out.println(url);
            response = makeRequest(url);

            process(txnType , monthYear ,  response.get("data").getAsJsonArray() , recordList, totalAmount, debits);

            pages++;
        }

        if (recordList.size() == 0){
            data.add(-1);
        }else {

            double avg = totalAmount[0] / debits[0];

            System.out.println("avg ::"+avg);

            recordList.forEach(record -> {
                System.out.println("amount ::"+record.amount+"|id::"+record.id);
            });

            data = recordList.stream().filter(record -> record.amount > avg).map(record -> record.id).collect(Collectors.toList());

            if (data.size() == 0){
                data.add(-1);
            }
        }

        Collections.sort(data);

        return data;
    }



    public static void process(String txnType, String monthYear, JsonArray data, List<Record> list, double[] totalAmount , int[] debits){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        data.forEach(jsonElement -> {

            JsonObject object =  jsonElement.getAsJsonObject();

            String am =  object.get("amount").getAsString();
            am = am.replaceAll("[$,]","");
            double a = Double.parseDouble(am);

            String format = formatDate(object.get("timestamp").getAsString(), formatter);

            if (object.get("txnType").getAsString().equalsIgnoreCase(txnType) && monthYear.equals(
                    format
            )) {


                Record record = new Record(object.get("id").getAsInt() ,a);

                list.add(record);
            }

            if (object.get("txnType").getAsString().equals("debit") && format.equals(monthYear)){
                System.out.println(object);
                totalAmount[0] += a;
                debits[0] += 1;

            }


        });

    }

   static class Record{
        int id ;
        double amount ;

        public Record(int id, double amount) {
            this.id = id;
            this.amount = amount;
        }
    }

    public static String formatDate(String timestamp , DateTimeFormatter formatter){
        LocalDateTime localDateTime =  LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timestamp)),
                ZoneId.systemDefault());
       return localDateTime.format(formatter);

    }

    public static JsonObject makeRequest(String baseUrl){

        try {

            URL url = new URL(baseUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

           return  JsonParser.parseString(content.toString()).getAsJsonObject();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return new JsonObject();

    }

    public static void main(String... args){

        try {
            Float f1 = new Float("3.0");

            int x = f1.intValue();
            byte b = f1.byteValue();

            double d = f1.doubleValue();

            System.out.println(x + b + d);
        }catch (NumberFormatException e){
            System.out.println("bad number");
        }

    }


}
