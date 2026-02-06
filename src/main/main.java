package main;

import java.util.Scanner;

public class main{
    public main(String[] args) {
        Bank bank = new Bank();
        bank.startAutoSaveProcessor(); // Start autosave processor in background
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Banking System ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Show Balance");
            System.out.println("6. Request Loan");
            System.out.println("7. Generate Statement");
            System.out.println("8. Financial Advisor");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter Account Number: ");
                    String accountNumber = scanner.nextLine();
                    System.out.print("Enter Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Initial Balance: ");
                    double balance = scanner.nextDouble();
                    bank.createAccount(accountNumber, name, balance);
                    break;
                case 2:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    System.out.print("Enter Amount to Deposit: ");
                    double depositAmount = scanner.nextDouble();
                    bank.deposit(accountNumber, depositAmount);
                    break;
                case 3:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    System.out.print("Enter Amount to Withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    bank.withdraw(accountNumber, withdrawAmount);
                    break;
                case 4:
                    System.out.print("Enter Sender Account Number: ");
                    String fromAccount = scanner.nextLine();
                    System.out.print("Enter Receiver Account Number: ");
                    String toAccount = scanner.nextLine();
                    System.out.print("Enter Amount to Transfer: ");
                    double transferAmount = scanner.nextDouble();
                    bank.transfer(fromAccount, toAccount, transferAmount);
                    break;
                case 5:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    bank.showBalance(accountNumber);
                    break;
                case 6:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    System.out.print("Enter Loan Amount: ");
                    double loanAmount = scanner.nextDouble();
                    System.out.print("Enter Interest Rate: ");
                    double interestRate = scanner.nextDouble();
                    bank.requestLoan(accountNumber, loanAmount, interestRate);
                    break;
                case 7:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    bank.generateStatement(accountNumber);
                    break;
                case 8:
                    System.out.print("Enter Account Number: ");
                    accountNumber = scanner.nextLine();
                    bank.financialAdvisor(accountNumber);
                    break;
                case 9:
                    System.out.println("Exiting... Thank you for using the Banking System!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
}