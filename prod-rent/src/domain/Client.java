package domain;

import utils.PackUtils;

import java.util.Arrays;

public class Client {

    private static final int MAX_PRODUCTS = 3;
    public static final int NAME_LIMIT = 10;
    public static final int SIZE = 8 + (NAME_LIMIT*2) + 4 + (MAX_PRODUCTS*8) + (MAX_PRODUCTS*4);

    private final long id;
    private final String name;
    private int balance;
    private final long[] rentedIds;
    private final int[] rentedUnits;
    // HINT: More instance variables will be needed

    /**
     * constructor per a crear un client amb els paràmetres indicats
     * @param id
     * @param name
     * @param balance
     */
    public Client(long id, String name, int balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.rentedIds = new long[MAX_PRODUCTS];
        this.rentedUnits = new int[MAX_PRODUCTS];

    }

    /**
     * mètode per a obtenir el id del client
     * @return id
     */
    public long getId() {
        return this.id;
    }

    /**
     * mètode per a obtenir el nom del client
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * mètode per a obtenir el balanç del client
     * @return balance
     */
    public int getBalance() {
        return this.balance;
    }

    /**
     * mètode per a afegir balanç al client
     * @param amount
     */
    public void addBalance(int amount) {
        if(amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * mètode per a restar balanç al client
     * @param amount
     */
    public void subBalance(int amount){
        if(this.balance >= amount && amount > 0){
            this.balance -= amount;
        }
    }

    /**
     * mètode per a comprovar si un client pot llogar un producte o no
     * recorrem l'array rentedIds per a mirar quants productes té ja llogats
     * @param idProduct
     * @return true si encara pot llogar un producte (productCount < 3), false altrament
     */
    public boolean canAddProduct(long idProduct) {
        int productCount = 0;
        for(int i = 0; i < MAX_PRODUCTS; i++){
            if(rentedIds[i] != 0){
                productCount++;
            }
        }
        return productCount < 3;
    }

    /**
     * mètode per saber si el client ja té un producte
     * busquem dins l'array rentedIds buscant un idProduct que coincideixi amb el donat
     * @param idProduct
     * @return true si el client té el producte dins l'array, false altrament
     */
    public boolean hasProduct(long idProduct){
        for(int i = 0; i < MAX_PRODUCTS; i++){
            if(rentedIds[i] == idProduct) {
                return true;
            }
        }
        return false;
    }

    /**
     * mètode per a llogar un producte, si aquest existeix l'incrementem en 1, si no existeix l'afegim a l'array
     * @param idProduct
     * @return true si s'ha pogut afegir el producte, false altrament
     */
    public boolean rentProduct(long idProduct) {
        for (int i = 0; i < MAX_PRODUCTS; i++) {
            if (rentedIds[i] == idProduct) {
                rentedUnits[i]++;
                return true;
            }
        }

        for (int i = 0; i < MAX_PRODUCTS; i++) {
            if (rentedIds[i] == 0) {
                rentedIds[i] = idProduct;
                rentedUnits[i] = 1;
                return true;
            }
        }

        return false;
    }

    /**
     * mètode per a retornar un producte,
     * el busquem dins l'array rentedIds i si el trobem el restem de l'array rentedUnits
     * @param idProduct
     * @return true si s'ha pogut retornar el producte, false altrament
     */
    public boolean returnProduct(long idProduct) {
        for(int i = 0; i < MAX_PRODUCTS; i++){
            if(rentedIds[i] == idProduct) {
                rentedUnits[i]--;
                return true;
            }
        }
        return false;
    }

    /**
     * mètode per a obtenir les unitats llogades d'un determinat producte
     * @param idProduct
     * @return unitats del producte
     */
    public int getRentedUnits(long idProduct) {
        for(int i = 0; i < MAX_PRODUCTS; i++){
            if(rentedIds[i] == idProduct) {
                return rentedUnits[i];
            }
        }
        return 0;
    }

    /**
     * contem els productes diferents llogats, si es 0 retornem directament
     * un array buit, si no, creem un array de la mida necessària i l'emplenem amb les id llogades
     * @return array long[] amb les id llogades
     */
    public long[] getRentedIds() {
        int count = 0;

        for (int i = 0; i < MAX_PRODUCTS; i++) {
            if (rentedUnits[i] > 0) {
                count++;
            }
        }

        if(count == 0){
            return new long[0];
        }

        long[] result = new long[count];
        int j = 0;
        for(int i = 0; i < MAX_PRODUCTS; i++){
            if(rentedUnits[i] > 0){
                result[j++] = rentedIds[i];
            }
        }

        return result;
    }

    /**
     * mètode per a convertir la informació d'un CLient a bytes, creem un array de bytes
     * i anem empaquetant els diferents paràmetres dins, cada cop afegim els bytes corresponents
     * al offset
     * @return bytes
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[SIZE];
        int offset = 0;

        PackUtils.packLong(id, bytes, offset);
        offset += 8;

        PackUtils.packLimitedString(name, NAME_LIMIT, bytes, offset);
        offset += 2*NAME_LIMIT;

        PackUtils.packInt(balance, bytes, offset);
        offset += 4;

        for(int i = 0; i < MAX_PRODUCTS; i++){
            PackUtils.packLong(rentedIds[i], bytes, offset);
            offset += 8;
        }
        for(int i = 0; i < MAX_PRODUCTS; i++){
            PackUtils.packInt(rentedUnits[i], bytes, offset);
            offset += 4;
        }
        return bytes;
    }

    /**
     * mètode per a transformar informació d'un client emmagatzemada en bytes a un objecte Client
     * desempaquetem els bytes i construïm un nou Client amb els paràmnetres obtinguts
     * @param record
     * @return client
     */
    public static Client fromBytes(byte[] record) {
        int offset = 0;

        long id = PackUtils.unpackLong(record, offset);
        offset += 8;

        String name = PackUtils.unpackLimitedString(NAME_LIMIT, record, offset);
        offset += 2*NAME_LIMIT;

        int balance = PackUtils.unpackInt(record, offset);
        offset += 4;

        Client client = new Client(id, name, balance);

        for(int i = 0; i < MAX_PRODUCTS; i++){
            client.rentedIds[i] = PackUtils.unpackLong(record, offset);
            offset += 8;
        }

        for(int i = 0; i < MAX_PRODUCTS; i++){
            client.rentedUnits[i] = PackUtils.unpackInt(record, offset);
            offset += 4;
        }

        return client;
    }

    public boolean isEqualTo(Client other) {
        if (id != other.id
                || !name.equals(other.name)
                || balance != other.balance) {
            return false;
        }
        long[] myProductIds = getRentedIds();
        long[] theirProductIds = other.getRentedIds();
        Arrays.sort(myProductIds);
        Arrays.sort(theirProductIds);
        int[] myStocks = getStocks(myProductIds);
        int[] theirStocks = getStocks(theirProductIds);
        return Arrays.equals(myProductIds, theirProductIds)
                && Arrays.equals(myStocks, theirStocks);
    }

    @Override
    public String toString() {
        long[] productIds = getRentedIds();
        Arrays.sort(productIds);
        int[] stocks = getStocks(productIds);
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", productIds=" + Arrays.toString(productIds) +
                ", stocks=" + Arrays.toString(stocks) +
                '}';
    }

    private int[] getStocks(long[] productIds) {
        int[] stocks = new int[productIds.length];
        for (int i = 0; i < stocks.length; i++) {
            stocks[i] = this.getRentedUnits(productIds[i]);
        }

        return stocks;
    }
}
