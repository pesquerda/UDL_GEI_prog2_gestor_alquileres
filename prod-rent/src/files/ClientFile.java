package files;

import domain.Client;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientFile {
    private final RandomAccessFile clients;

    public ClientFile(String fileName) throws IOException {
        this.clients = new RandomAccessFile(fileName, "rw");
    }

    /**
     * mètode per a escriure un client al fitxer
     * col·loca el punter a la posició corresponent i escriu les dades del client
     * @param client
     * @throws IOException
     */
    public void write(Client client) throws IOException {
        long pos = (client.getId() - 1) * Client.SIZE;
        clients.seek(pos);
        clients.write(client.toBytes());
    }

    /**
     * mètode per a llegir un client del fitxer donat el seu identificador
     * comprova que l'identificador sigui vàlid i retorna el client corresponent
     * @param id
     * @return client
     * @throws IOException id no vàlid
     */
    public Client read(long id) throws IOException {
        long pos = (id - 1) * Client.SIZE;
        if(!isValid(id)){
            throw new IOException("Invalid client ID: " + id);
        }
        clients.seek(pos);
        byte[] buffer = new byte[Client.SIZE];
        clients.readFully(buffer);
        return Client.fromBytes(buffer);
    }

    /**
     * mètode per a obtenir el següent identificador disponible
     * calcula id basant-se en la longitud actual del fitxer
     * @return next id
     * @throws IOException
     */
    public long nextId() throws IOException {
        return clients.length() / Client.SIZE + 1;
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
        long totalRecords = clients.length() / Client.SIZE;
        return id <= totalRecords;
    }

    /**
     * mètode per a buidar el contingut del fitxer
     * posa la longitud del fitxer a zero
     * @throws IOException
     */
    public void reset() throws IOException {
        clients.setLength(0);
    }

    /**
     * mètode per a tancar el fitxer
     * @throws IOException
     */
    public void close() throws IOException {
        clients.close();
    }
}
