package domain;

import utils.PackUtils;

public class Product {

    public static final int DESCRIPTION_LIMIT = 20;
    public static final int SIZE = 8 +(DESCRIPTION_LIMIT*2) + 4*2;

    private final long id;
    private final String description;
    private final int price;
    private int stock;

    /**
     * constructor per a crear un producte amb els paràmetres indicats
     * @param id
     * @param description
     * @param price
     * @param stock 
     */
    public Product(long id, String description, int price, int stock) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    /**
     * mètode per a obtenir l'identificador del producte
     * @return id
     */
    public long getId() {
        return this.id;
    }

    /**
     * mètode per a obtenir la descripció del producte
     * @return description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * mètode per a obtenir el preu del producte
     * @return price
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * mètode per a obtenir les unitats en estoc del producte
     * @return stock
     */
    public int getStock() {
        return this.stock;
    }

    /**
     * mètode per a incrementar en 1 el nombre d'unitats en estoc
     */
    public void incrementStock() {
        this.stock++;
    }

    /**
     * mètode per a decrementar en 1 el nombre d'unitats en estoc
     * només decrementarà si hi ha més d'1 unitat disponible
     */
    public void decrementStock() {
        if(this.stock > 1) {
            this.stock--;
        }
    }

    /**
     * mètode per a convertir la informació d’un producte a bytes
     * creem un array de bytes i hi afegim els valors en l'ordre corresponent
     * @return array de bytes amb la informació del producte
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[SIZE];
        int offset = 0;

        PackUtils.packLong(id, bytes, offset);
        offset += 8;

        PackUtils.packLimitedString(description, DESCRIPTION_LIMIT,  bytes, offset);
        offset += 2*DESCRIPTION_LIMIT;

        PackUtils.packInt(price, bytes, offset);
        offset += 4;

        PackUtils.packInt(stock, bytes, offset);
        offset += 4;

        return bytes;
    }

    /**
     * mètode per a crear un objecte Product a partir d’un array de bytes
     * desempaquetem els bytes i construïm el producte amb les dades obtingudes
     * @param record
     * @return producte creat a partir de la informació desempaquetada
     */
    public static Product fromBytes(byte[] record) {
        int offset = 0;

        long id = PackUtils.unpackLong(record, offset);
        offset += 8;

        String description = PackUtils.unpackLimitedString(DESCRIPTION_LIMIT, record, offset);
        offset += 2*DESCRIPTION_LIMIT;

        int price = PackUtils.unpackInt(record, offset);
        offset += 4;

        int stock = PackUtils.unpackInt(record, offset);

        Product product = new Product(id, description, price, stock);
        return product;
    }

    public boolean isEqualTo(Product other) {
        return id == other.id
                && description.equals(other.description)
                && price == other.price
                && stock == other.stock;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
