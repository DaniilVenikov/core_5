import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException {
        File file1 = new File("data.csv");
        File file2 = new File("data.json");
        File file3 = new File("data.xml");
        File file4 = new File("data2.json");


        //1-АЯ ЗАДАЧА
        /*
        создаём список, который будет содержать массивы строк, исходные строки разбиваем на значения по запятой
        далее берём массивы из списка и записываем значения в файл data.csv с помощью метода writeCSVFile
         */
        List<String[]> listEmployee = new ArrayList<>();
        listEmployee.add("1,John,Smith,USA,25".split(","));
        listEmployee.add("2,Ivan,Petrov,RU,23".split(","));

        for(String[] employee : listEmployee){
            writeCSVFile(employee, file1);
        }

        /*
        создаём массив columnMapping, который содержит строчки с информацией о предназначении колонок в CSV файле
        этот массив будем использовать для передачи в метод как параметра, который нужен для создания стратегии
         */
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        //парсим значения колонок из CSV файла в список объктов Employee
        List<Employee> staff = parseCSV(columnMapping, file1.getName());

        //полученный список преобразовываем в строчку в формате json
        String json = listToJson(staff);

        //записываем полученную строку в файл data.json
        writeString(json, file2);


        //2-АЯ ЗАДАЧА
        //метод createXMLFile созадаём XML-файл c нужными данными
        createXMLFile(file3);

        /*
        с помощью метода parseXML парсим значения из файла data.xml в список объектов Employee
        далее передаём этот список как параметр в метод listToJson, который преобразовыет наш список объетов Employee
        в строку json
         */
        String json_2 =  listToJson(parseXML(file3.getPath()));

        //записываем наш результат в файл data2.json
        writeString(json_2, file4);


        //3-Я ЗАДАЧА
        //читаем данные из файла data2.json и возвращаем результат в виде строки
        String json_3 = readJson(file4.getPath());
        //прочитанный json преобразуем в список сотрудников
        List<Employee> employees = jsonToList(json_3);
        System.out.println(employees);




    }

    public static void createFile(File file){
        try {
            if (file.createNewFile()) {
                System.out.println("Файл успешно создан");
            } else {
                System.out.println("Не удалось создать файл");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void writeCSVFile(String[] text, File file){
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(file, true))){
            csvWriter.writeNext(text);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName){
        try(CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csvToBean = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            return csvToBean.parse();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String listToJson(List<Employee> list){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        Type listType = new TypeToken<List<Employee>>(){}.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String text, File file){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            bw.write(text);
            bw.flush();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void createXMLFile(File file){
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("staff");
            document.appendChild(root);

            Element employee = document.createElement("employee");
            employee.setAttribute("id", "1");
            employee.setAttribute("firstName", "John");
            employee.setAttribute("lastName", "Smith");
            employee.setAttribute("country", "USA");
            employee.setAttribute("age", "25");
            root.appendChild(employee);

            Element secondEmployee = document.createElement("employee");
            secondEmployee.setAttribute("id", "2");
            secondEmployee.setAttribute("firstName", "Ivan");
            secondEmployee.setAttribute("lastName", "Petrov");
            secondEmployee.setAttribute("country", "RU");
            secondEmployee.setAttribute("age", "23");
            root.appendChild(secondEmployee);

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, streamResult);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public static List<Employee> parseXML(String pathFile){
        List<Employee> employees = new ArrayList<>();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(pathFile);

            Node root = doc.getDocumentElement();

            NodeList nodeList = root.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()){
                    Element employee = (Element) node;
                    employees.add(new Employee(Long.parseLong(employee.getAttribute("id")), employee.getAttribute("firstName"),
                            employee.getAttribute("lastName"), employee.getAttribute("country"),
                            Integer.parseInt(employee.getAttribute("age"))));
                }
            }
            return employees;

        } catch (Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public static String readJson(String pathFile){
        StringBuilder stringBuilder = new StringBuilder();
        String s;
        try(BufferedReader bw = new BufferedReader(new FileReader(pathFile))){
            while ((s = bw.readLine()) != null){
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        } catch (IOException ex){
            return ex.getMessage();
        }
    }

    public static List<Employee> jsonToList(String text){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        Type listType = new TypeToken<List<Employee>>(){}.getType();
        return gson.fromJson(text, listType);
    }
}
