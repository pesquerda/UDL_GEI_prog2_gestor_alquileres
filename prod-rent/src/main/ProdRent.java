package main;

import acm.program.CommandLineProgram;
import domain.Client;
import domain.Product;
import files.ClientFile;
import files.LogFile;
import files.ProductFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class ProdRent extends CommandLineProgram {

    private static final String PRODUCTS = "productsDB.dat";
    private static final String CLIENTS = "clientsDB.dat";
    private String movements;
    private String logger;

    private BufferedReader movementsFile;
    private LogFile logFile;
    private ProductFile productsDB;
    private ClientFile clientsDB;

    public static void main(String[] args) {
        new ProdRent().start(args);
    }

    public void run() {
        try {
            askFileNames();
            openFiles();
            resetFiles();
            processMovements();
        } catch (IOException ex) {
            println("ERROR");
            ex.printStackTrace();
        } finally {
            try {
                closeFiles();
            } catch (IOException ex) {
                println("ERROR Closing");
                ex.printStackTrace();
            }
        }
    }

    /**
     * mètode per a demanar els noms dels fitxers a l'usuari
     */
    private void askFileNames() {
        movements = readLine("Nom del fitxer de moviments (.txt): ");
        logger = readLine("Nom del fitxer bitàcora (.out): ");
    }

    /**
     * mètode per a obrir els fitxers necessaris
     * @throws IOException si hi ha un error obrint fitxers
     */
    private void openFiles() throws IOException {
        movementsFile = new BufferedReader(new FileReader(movements));
        logFile = new LogFile(logger);
        productsDB = new ProductFile(PRODUCTS);
        clientsDB = new ClientFile(CLIENTS);
    }

    /**
     * mètode per a tancar els fitxers oberts
     * @throws IOException si hi ha un error tancant fitxers
     */
    private void closeFiles() throws IOException {
        if (movementsFile != null) movementsFile.close();
        if (logFile != null) logFile.close();
        if (productsDB != null) productsDB.close();
        if (clientsDB != null) clientsDB.close();
    }

    /**
     * mètode per a buidar els fitxers de productes i clients
     * @throws IOException
     */
    private void resetFiles() throws IOException {
        productsDB.reset();
        clientsDB.reset();
    }

    /**
     * mètode per a processar totes les línies del fitxer de moviments
     * @throws IOException
     */
    private void processMovements() throws IOException {
        String line;
        while((line = movementsFile.readLine()) != null){
            processMovement(line);
        }
    }

    /**
     * mètode per a processar una línia concreta del fitxer de moviments
     * @param line
     * @throws IOException
     */
    private void processMovement(String line) throws IOException {
        var tokenizer = new StringTokenizer(line, ",");
        if(!tokenizer.hasMoreTokens()) return;

        String operation = tokenizer.nextToken().trim().toUpperCase();
        try {
            if (operation.equals("ALTA_PRODUCTO")) {
                processAltaProducto(tokenizer);
            } else if (operation.equals("ALTA_CLIENTE")) {
                processAltaCliente(tokenizer);
            } else if (operation.equals("INFO_PRODUCTO")) {
                processInfoProduct(tokenizer);
            } else if (operation.equals("INFO_CLIENTE")) {
                processInfoClient(tokenizer);
            } else if (operation.equals("ALQUILAR")) {
                processAlquilar(tokenizer);
            } else if (operation.equals("DEVOLVER")) {
                processDevolver(tokenizer);
            } else {
                logFile.unknownOperation(operation);
            }
        }catch (Exception e){
            logFile.unknownOperation(operation);
        }
    }

    /**
     * mètode per a processar l'alta d'un nou producte
     * apliquem filtres i si tot està ok procedim
     * @param tokenizer
     * @throws IOException
     */
    private void processAltaProducto(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        String description = tokenizer.nextToken();

        if(!tokenizer.hasMoreTokens()) return;
        int price = Integer.parseInt(tokenizer.nextToken());

        if(!tokenizer.hasMoreTokens()) return;
        int stock = Integer.parseInt(tokenizer.nextToken());

        if(price <= 0){
            logFile.errorPriceCannotBeNegativeOrZero(description, price);
            return;
        }
        if(stock <= 0){
            logFile.errorStockCannotBeNegativeOrZero(description, price);
            return;
        }

        long id = productsDB.nextId();
        var product = new Product(id, description, price, stock);
        productsDB.write(product);
        logFile.okNewProduct(product);
    }

    /**
     * mètode per a processar l'alta d'un nou client
     * apliquem filtres i si tot està ok procedim
     * @param tokenizer
     * @throws IOException
     */
    private void processAltaCliente(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        String name = tokenizer.nextToken();

        if(!tokenizer.hasMoreTokens()) return;
        int balance = Integer.parseInt(tokenizer.nextToken());

        if(balance <= 0){
            logFile.errorBalanceCannotBeNegativeOrZero(name, balance);
            return;
        }

        long id = clientsDB.nextId();
        var client = new Client(id, name, balance);
        clientsDB.write(client); // faltava aquesta línia
        logFile.okNewClient(client); // opcional: si vols registrar també l'alta del client
    }
    /**
     * mètode per a mostrar la informació d'un producte
     * mirem si id es vàlida
     * @param tokenizer
     * @throws IOException
     */
    private void processInfoProduct(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        long id = Long.parseLong(tokenizer.nextToken());
        if(!productsDB.isValid(id)){
            logFile.errorInvalidProductId(id);
            return;
        }
        Product product = productsDB.read(id);
        logFile.infoProduct(product);
    }

    /**
     * mètode per a mostrar la informació d'un client i els seus productes llogats
     * mirem si id es vàlida
     * creem array amb les id llogades
     * @param tokenizer
     * @throws IOException
     */
    private void processInfoClient(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        long id = Long.parseLong(tokenizer.nextToken());

        if(!clientsDB.isValid(id)){
            logFile.errorInvalidClientId(id);
            return;
        }

        Client client = clientsDB.read(id);
        long[] rentedIds = client.getRentedIds();
        var rentedProducts = new Product[rentedIds.length];

        for(int i = 0; i < rentedIds.length; i++){
            rentedProducts[i] = productsDB.read(rentedIds[i]);
        }

        logFile.infoClient(client, rentedProducts);
    }

    /**
     * mètode per a processar el lloguer d'un producte
     * apliquem filtres per comprovar que el client té els requisits necessàris
     * @param tokenizer
     * @throws IOException
     */
    private void processAlquilar(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        long idClient = Long.parseLong(tokenizer.nextToken());

        if(!tokenizer.hasMoreTokens()) return;
        long idProduct = Long.parseLong(tokenizer.nextToken());

        Client client = clientsDB.read(idClient);
        Product product = productsDB.read(idProduct);

        if(!clientsDB.isValid(idClient)){
            logFile.errorInvalidClientId(idClient);
            return;
        }
        if(!productsDB.isValid(idProduct)){
            logFile.errorInvalidProductId(idProduct);
            return;
        }
        if(product.getStock() == 0){
            logFile.errorCannotRentProductWithNoStock(product);
            return;
        }
        if(client.getBalance() < product.getPrice()){
            logFile.errorClientHasNotEnoughFundsToRentProduct(client, product);
            return;
        }
        if(!client.canAddProduct(idProduct)){
            logFile.errorClientCannotAddProduct(client, product);
            return;
        }

        client.rentProduct(idProduct);
        client.subBalance(product.getPrice());
        product.decrementStock();

        clientsDB.write(client);
        productsDB.write(product);

        logFile.okRent(client, product);
    }

    /**
     * mètode per a processar la devolució d’un producte per part d’un client
     * mriem si les id son vàlides i si el client té aquell producte
     * @param tokenizer
     * @throws IOException
     */
    private void processDevolver(StringTokenizer tokenizer) throws IOException {
        if(!tokenizer.hasMoreTokens()) return;
        long idClient = Long.parseLong(tokenizer.nextToken());

        if(!tokenizer.hasMoreTokens()) return;
        long idProduct = Long.parseLong(tokenizer.nextToken());

        Client client = clientsDB.read(idClient);
        Product product = productsDB.read(idProduct);

        long[] rentedIds = client.getRentedIds();
        var rentedProducts = new Product[rentedIds.length];

        if(!clientsDB.isValid(idClient)){
            logFile.errorInvalidClientId(idClient);
            return;
        }
        if(!productsDB.isValid(idProduct)){
            logFile.errorInvalidProductId(idProduct);
            return;
        }
        if (!client.hasProduct(idProduct)) {
            logFile.errorClientHasNotProduct(client, idProduct);
            return;
        }

        client.returnProduct(idProduct);
        product.incrementStock();

        clientsDB.write(client);
        productsDB.write(product);

        logFile.okRent(client, product);
    }
}
