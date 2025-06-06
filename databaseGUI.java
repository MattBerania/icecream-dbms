import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class DatabaseGUI {

    private static Connection connection;

    public static void main(String[] args) {

        String dbURL = "jdbc:oracle:thin:USERNAME/PASSWORD@oracle.scs.ryerson.ca:1521:orcl";
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(dbURL);
            // connection = DriverManager.getConnection(dbURL, username, password);
            if (connection != null) {
                System.out.println("Connected to the database!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            return;
        }

        // ImageIcon image = new ImageIcon("iceCream.png");

        // creates main frame that will contain all the componenets
        JFrame mainFrame = new JFrame("Oracle Database GUI");
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // creating a panel containing multiple columns for each tables
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // adds form for the custom SQL queries
        JPanel sqlFormPanel = new JPanel();
        sqlFormPanel.setLayout(new BorderLayout());

        // creates text form for custom user input SQL queries
        JTextArea sqlInputArea = new JTextArea(5, 50);
        sqlInputArea.setLineWrap(true);
        sqlInputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(sqlInputArea);

        // button that executes the queries in the text form
        JButton executeButton = new JButton("Execute SQL");
        executeButton.addActionListener(e -> executeSQL(sqlInputArea.getText()));

        sqlFormPanel.add(new JLabel("Enter SQL Statement:"), BorderLayout.NORTH);
        sqlFormPanel.add(scrollPane, BorderLayout.CENTER); // adds scroll option to the text form
        sqlFormPanel.add(executeButton, BorderLayout.SOUTH); // adds the execute button below the form 

        mainPanel.add(sqlFormPanel); // adds the user input text form to the main panel
        

        // gets all table names from database and displayes them 
        ArrayList<String> tables = getTableNames();
        JPanel tablePanelContainer = new JPanel();
        tablePanelContainer.setLayout(new GridLayout(0, 4, 10, 10)); // uses GridLayout to put 4 columns per row, for aesthetics


        for (String tableName : tables) {
            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new FlowLayout());

            // adding label for each table
            JLabel tableLabel = new JLabel(tableName);
            tablePanel.add(tableLabel);

            // declaring buttons for each table
            JButton viewButton = new JButton("View");
            JButton insertButton = new JButton("Insert");
            JButton deleteButton = new JButton("Delete");

            // adding the buttons to the table panels
            tablePanel.add(viewButton);
            tablePanel.add(insertButton);
            tablePanel.add(deleteButton);

            // action listeners for each button to run the corresponding functions
            viewButton.addActionListener(e -> viewTable(tableName));
            insertButton.addActionListener(e -> showInsertForm(tableName));
            deleteButton.addActionListener(e -> showDeleteForm(tableName));

            // adds the table panel to the tabel panel container
            tablePanelContainer.add(tablePanel);
        }

        // adds the tabel panel container to the main panel
        mainPanel.add(tablePanelContainer);

        // panel for the create and drop table buttons
        JPanel createDropPanel = new JPanel();
        createDropPanel.setLayout(new FlowLayout());

        // declaring create and drop buttons
        JButton createTableButton = new JButton("Create Table");
        JButton dropTableButton = new JButton("Drop Table");

        // adds the buttons to the main create and drop panel
        createDropPanel.add(createTableButton);
        createDropPanel.add(dropTableButton);

        // connects button to its corresponding functions
        createTableButton.addActionListener(e -> showCreateTableForm());
        dropTableButton.addActionListener(e -> showDropTableForm());

        // adds the create and drop panel to the main panel
        mainPanel.add(createDropPanel);

        // declaring Custom Query Button
        JButton queryButton = new JButton("Open Query Panel");
        queryButton.addActionListener(e -> openQueryPanel()); // connects button to its corresponding functions
        mainPanel.add(queryButton); // adds query button to the main panel

        // adds the main panel to the main frame and sets the main frame visibility to true
        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    // FUNCTIONS --------------------------------------------------------------------------------------------------------------------------------

    // function for the execute button for the user input SQL
    private static void executeSQL(String sqlQuery) {
        try (Statement stmt = connection.createStatement()) { // declares statement to use in the database
            ResultSet rs = stmt.executeQuery(sqlQuery);  // declares a ResultSet variable which executes the query 'sqlQuery', which is the inputted SQL string
            StringBuilder result = new StringBuilder(); // declares StringBuilder result which is being used as an appended String that will be added to the output
            while (rs.next()) {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    result.append(rs.getString(i)).append(" ");
                }
                result.append("\n");
            }
            JOptionPane.showMessageDialog(null, result.toString(), "SQL Query Results", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // function that fetches all the tables from the database connection
    private static ArrayList<String> getTableNames() {
        ArrayList<String> tableNames = new ArrayList<>(); // creates an String ArrayLlist, which will be where the tables will be stored in.
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT table_name FROM all_tables WHERE owner = 'MBERANIA'");
            while (rs.next()) {
                tableNames.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableNames;
    }


    private static void viewTable(String tableName) {
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);


            // gets the meta data to fetch the column names
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // creates array to store all the column names
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }

            // creates an array to store the data for aesthetics
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

            // fills the tabel with the data
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }

            // creates Jtable which takes in the tabel model that was created earlier 
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // sets preffered size to make all the data visible
            scrollPane.setPreferredSize(new java.awt.Dimension(1000, 200));

            // Show the table in a JOptionPane dialog
            JOptionPane.showMessageDialog(null, scrollPane, "Search Results", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // function for showing the Insert form, which takes in the table name as a string
    private static void showInsertForm(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            JPanel insertPanel = new JPanel();
            insertPanel.setLayout(new GridLayout(columnCount + 1, 2));
    
            JTextField[] textFields = new JTextField[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                insertPanel.add(new JLabel(metaData.getColumnName(i)));
                textFields[i - 1] = new JTextField(20);
                insertPanel.add(textFields[i - 1]);
            }
    
            // creates an int which takes in user input on what data to insert in 
            int option = JOptionPane.showConfirmDialog(null, insertPanel, "Insert into " + tableName, JOptionPane.OK_CANCEL_OPTION);
        
            
            // if user clicked Cancel, just return without doing anything
            if (option == JOptionPane.CANCEL_OPTION) {
                return;
            }
    
            // if user clicked OK, then run the SQL statement
            if (option == JOptionPane.OK_OPTION) {
                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    columns.append(metaData.getColumnName(i + 1));
                    values.append("'").append(textFields[i].getText()).append("'");
                    if (i < columnCount - 1) {
                        columns.append(", ");
                        values.append(", ");
                    }
                }
    
                String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
                stmt.executeUpdate(query); // execute update just updates the table
                JOptionPane.showMessageDialog(null, "Record inserted successfully.", "Insert Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    // function for the delete form
    private static void showDeleteForm(String tableName) {
        JPanel deletePanel = new JPanel();
        deletePanel.setLayout(new GridLayout(2, 2));
    
        JTextField columnField = new JTextField(20);
        JTextField valueField = new JTextField(20);
    
        deletePanel.add(new JLabel("Column name:"));
        deletePanel.add(columnField);
        deletePanel.add(new JLabel("Value to delete:"));
        deletePanel.add(valueField);
    
        int option = JOptionPane.showConfirmDialog(null, deletePanel, "Delete from " + tableName, JOptionPane.OK_CANCEL_OPTION);
    
        // checks if the user pressed OK
        if (option == JOptionPane.OK_OPTION) {
            String column = columnField.getText();
            String value = valueField.getText();
    
            try (Statement stmt = connection.createStatement()) {
                String query = "DELETE FROM " + tableName + " WHERE " + column + " = '" + value + "'";
                stmt.executeUpdate(query);
                JOptionPane.showMessageDialog(null, "Record deleted successfully.", "Delete Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // function for showing the create table form 
    private static void showCreateTableForm() {
        JPanel createTablePanel = new JPanel();
        createTablePanel.setLayout(new GridLayout(3, 2));
    
        JTextField tableNameField = new JTextField(20);
        JTextField columnsField = new JTextField(20);
    
        createTablePanel.add(new JLabel("Table name:"));
        createTablePanel.add(tableNameField);
        createTablePanel.add(new JLabel("Columns (column1 type1, column2 type2):"));
        createTablePanel.add(columnsField);
    
        int option = JOptionPane.showConfirmDialog(null, createTablePanel, "Create New Table", JOptionPane.OK_CANCEL_OPTION);
    
        if (option == JOptionPane.OK_OPTION) {
            String tableName = tableNameField.getText();
            String columns = columnsField.getText();
    
            try (Statement stmt = connection.createStatement()) {
                String query = "CREATE TABLE " + tableName + " (" + columns + ")";
                stmt.executeUpdate(query);
                JOptionPane.showMessageDialog(null, "Table created successfully.", "Create Table", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // function for showing drop table form
    private static void showDropTableForm() {
        JTextField tableNameField = new JTextField(20);
    
        JPanel dropTablePanel = new JPanel();
        dropTablePanel.add(new JLabel("Table name:"));
        dropTablePanel.add(tableNameField);
    
        // user input
        int option = JOptionPane.showConfirmDialog(null, dropTablePanel, "Drop Table", JOptionPane.OK_CANCEL_OPTION);
    
        if (option == JOptionPane.OK_OPTION) {
            String tableName = tableNameField.getText();
            try (Statement stmt = connection.createStatement()) {
                String query = "DROP TABLE " + tableName;
                stmt.executeUpdate(query);
                JOptionPane.showMessageDialog(null, "Table dropped successfully.", "Drop Table", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    

    // function for custom query panel
    private static void openQueryPanel() {
        JFrame customQueryFrame = new JFrame("Custom Query Panel");
        customQueryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        customQueryFrame.setSize(400, 300);

        JPanel customQueryPanel = new JPanel();
        customQueryPanel.setLayout(new GridLayout(0, 1)); // sets layout for the buttons

        // adds buttons for custom SQL queries
        addQueryButton(customQueryPanel, "Client Avg Order Price and Products Ordered", 
                       "SELECT c.clientID, c.clientName, AVG(o.orderPrice) AS AvgOrderPrice, COUNT(oi.prodID) AS TotalProductsOrdered "
                       + "FROM client c "
                       + "JOIN orders o ON c.clientID = o.clientID "
                       + "JOIN orderitem oi ON o.orderID = oi.orderID "
                       + "GROUP BY c.clientID, c.clientName "
                       + "ORDER BY AvgOrderPrice DESC");

        addQueryButton(customQueryPanel, "Ingredients with Quantity > 200", 
                       "SELECT ingredientname, COUNT(*) AS TotalIngredients "
                       + "FROM ingredient "
                       + "WHERE ingredientquantity > 200 "
                       + "GROUP BY ingredientname "
                       + "ORDER BY TotalIngredients");

        addQueryButton(customQueryPanel, "Product Avg Price", 
                       "SELECT prodname, AVG(prodprice) AS AvgProductPrice "
                       + "FROM product "
                       + "GROUP BY prodname");

        addQueryButton(customQueryPanel, "Client Total Quantity Ordered (by Client)", 
                       "SELECT c.clientID, c.clientName, SUM(oi.quantity) AS TotalQuantityOrdered "
                       + "FROM client c "
                       + "JOIN orders o ON c.clientID = o.clientID "
                       + "JOIN orderitem oi ON o.orderID = oi.orderID "
                       + "JOIN product p ON oi.prodID = p.prodID "
                       + "GROUP BY c.clientID, c.clientName "
                       + "ORDER BY TotalQuantityOrdered DESC");

        addQueryButton(customQueryPanel, "Client Total Quantity Ordered (Another Query)", 
                       "SELECT c.clientid, c.clientname, SUM(oi.quantity) AS totalquantityordered "
                       + "FROM client c "
                       + "JOIN orders o ON c.clientid = o.clientid "
                       + "JOIN orderitem oi ON o.orderid = oi.orderid "
                       + "GROUP BY c.clientid, c.clientname "
                       + "ORDER BY totalquantityordered DESC");

        // adds the custom query panel the the main custom query frame
        customQueryFrame.add(customQueryPanel);
        customQueryFrame.setVisible(true);
    }

    private static void addQueryButton(JPanel panel, String buttonText, String sqlQuery) {
        JButton queryButton = new JButton(buttonText);
        queryButton.addActionListener(e -> executeCustomQuery(sqlQuery));
        panel.add(queryButton);
    }

    private static void executeCustomQuery(String query) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            int columnCount = rs.getMetaData().getColumnCount();
            StringBuilder result = new StringBuilder();
            String[] columnNames = new String[columnCount];


            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rs.getMetaData().getColumnName(i);
            }

            /// creates a TableModel using the column names. 
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);


            // while loop for adding the data to the table
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }

            // creates JTable using the table model from before
            JTable table = new JTable(tableModel);

            // adds scroll option
            JScrollPane scrollPane = new JScrollPane(table);
            // sets preffered size to make all the data visible
            scrollPane.setPreferredSize(new java.awt.Dimension(1000, 200));

            // error handling if theres no data
            if (tableModel.getRowCount() > 0) {
                JOptionPane.showMessageDialog(null, scrollPane, "Query Results", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No results found for the query.", "Query Results", JOptionPane.INFORMATION_MESSAGE);
            }

        // error handling
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error executing query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
