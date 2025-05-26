package files;

import domain.Product;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ProductFile {
    private final RandomAccessFile products;

    /**
     * constructor per a crear un fitxer de productes amb el nom indicat
     * @param fileName
     * @throws IOException
     */
    public ProductFile(String fileName) throws IOException {
        this.products = new RandomAccessFile(fileName, "rw"); //read/write mode
    }

    /**
     * mètode per a escriure un producte al fitxer
     * col·loca el punter a la posició corresponent i escriu les dades del producte
     * @param product
     * @throws IOException
     */
    public void write(Product product) throws IOException {
        long pos = (product.getId() - 1) * Product.SIZE;
        products.seek(pos);
        products.write(product.toBytes());
    }

    /**
     * mètode per a llegir un producte del fitxer donat el seu identificador
     * comprova que l'identificador sigui vàlid i retorna el producte corresponent
     * @param id
     * @return product
     * @throws IOException si id no és vàlid
     */
    public Product read(long id) throws IOException {
        long pos = (id - 1) * Product.SIZE;
        if(!isValid(id)){
            throw new IOException("Invalid product ID: " + id);
        }
        products.seek(pos);
        byte[] buffer = new byte[Product.SIZE];
        products.readFully(buffer);
        return Product.fromBytes(buffer);
    }

    /**
     * mètode per a obtenir el següent identificador disponible
     * calcula id basant-se en la longitud actual del fitxer
     * @return next id
     * @throws IOException
     */
    public long nextId() throws IOException {
        return products.length() / Product.SIZE + 1;
    }

    /**
     * mètode per a comprovar si un identificador és vàlid
     * verifica que estigui dins del rang de registres existents
     * @param id
     * @return cert si és vàlid, fals altrament
     * @throws IOException
     */
    public boolean isValid(long id) throws IOException {
        if(id < 1){
            return false;
        }
        long totalRecords = products.length() / Product.SIZE;
        return id <= totalRecords;
    }

    /**
     * mètode per a buidar el contingut del fitxer
     * estableix la longitud del fitxer a zero
     * @throws IOException
     */
    public void reset() throws IOException {
        products.setLength(0);
    }

    /**
     * mètode per a tancar el fitxer
     * @throws IOException
     */
    public void close() throws IOException {
        products.close();
    }
}
