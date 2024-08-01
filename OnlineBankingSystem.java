import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

abstract class BankAccount {
    protected String accountNumber;
    protected double balance;
    protected List<String> transactions;

    public BankAccount(String accountNumber) {
        this.accountNumber = accountNumber;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public synchronized void deposit(double amount) {
        balance += amount;
        String transaction = getCurrentTime() + " | Deposit: $" + amount + " | Balance: $" + balance;
        transactions.add(transaction);
        saveTransaction(transaction);
    }

    public synchronized void withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            String transaction = getCurrentTime() + " | Withdraw: $" + amount + " | Balance: $" + balance;
            transactions.add(transaction);
            saveTransaction(transaction);
        } else {
            JOptionPane.showMessageDialog(null, "Insufficient balance.");
        }
    }

    public synchronized double checkBalance() {
        return balance;
    }

    public void seeStatement() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(accountNumber + "_statement.txt"))) {
            for (String transaction : transactions) {
                writer.write(transaction);
                writer.newLine();
            }
            JOptionPane.showMessageDialog(null, "Statement saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    protected void saveTransaction(String transaction) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(accountNumber + "_transactions.txt", true))) {
            writer.write(transaction);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SavingsAccount extends BankAccount {
    public SavingsAccount(String accountNumber) {
        super(accountNumber);
    }
}

class CheckingAccount extends BankAccount {
    public CheckingAccount(String accountNumber) {
        super(accountNumber);
    }
}

class User {
    private String username;
    private String password;
    private BankAccount account;

    public User(String username, String password, BankAccount account) {
        this.username = username;
        this.password = password;
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public BankAccount getAccount() {
        return account;
    }
}

public class OnlineBankingSystem extends JFrame {
    private List<User> users;
    private User loggedInUser;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField amountField;
    private JTextArea statementArea;

    public OnlineBankingSystem() {
        users = new ArrayList<>();
        initializeUsers();

        setTitle("Online Banking System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        loginPanel.add(loginButton);

        add(loginPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(3, 2));
        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> deposit());
        mainPanel.add(depositButton);
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> withdraw());
        mainPanel.add(withdrawButton);
        JButton checkBalanceButton = new JButton("Check Balance");
        checkBalanceButton.addActionListener(e -> checkBalance());
        mainPanel.add(checkBalanceButton);
        JButton seeStatementButton = new JButton("See Statement");
        seeStatementButton.addActionListener(e -> seeStatement());
        mainPanel.add(seeStatementButton);
        amountField = new JTextField();
        mainPanel.add(new JLabel("Amount:"));
        mainPanel.add(amountField);

        add(mainPanel, BorderLayout.CENTER);

        statementArea = new JTextArea();
        add(new JScrollPane(statementArea), BorderLayout.SOUTH);
    }

    private void initializeUsers() {
        users.add(new User("user1", "password1", new SavingsAccount("123456789")));
        users.add(new User("user2", "password2", new CheckingAccount("987654321")));
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                JOptionPane.showMessageDialog(this, "Login successful.");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid username or password.");
    }

    private void deposit() {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText());
            loggedInUser.getAccount().deposit(amount);
            JOptionPane.showMessageDialog(this, "Deposited: $" + amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        }
    }

    private void withdraw() {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText());
            loggedInUser.getAccount().withdraw(amount);
            JOptionPane.showMessageDialog(this, "Withdrawn: $" + amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        }
    }

    private void checkBalance() {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }
        double balance = loggedInUser.getAccount().checkBalance();
        JOptionPane.showMessageDialog(this, "Current balance: $" + balance);
    }

    private void seeStatement() {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }
        loggedInUser.getAccount().seeStatement();
        statementArea.setText("");
        for (String transaction : loggedInUser.getAccount().transactions) {
            statementArea.append(transaction + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new OnlineBankingSystem().setVisible(true);
        });
    }
}
