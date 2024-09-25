package msm;

import publickeycipher.PublicKeyCipher;
import util.Util;

import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class ClienteMensajeria {
    public static final String SERVER = "localhost";
    public static final int PORT = 3400;

    private static final Scanner SCANNER = new Scanner(System.in);

    private PrintWriter toNetwork;
    private BufferedReader fromNetwork;

    private Socket clientSideSocket;

    private String server;
    private int port;

    private  KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PublicKeyCipher cipher;

    private String llavePublicaMensaje;


    public ClienteMensajeria() throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.server = SERVER;
        this.port = PORT;

        this.keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        this.keyPair = keyPairGenerator.generateKeyPair();
        this.publicKey = keyPair.getPublic();

        this.privateKey = keyPair.getPrivate();
        this.cipher = new PublicKeyCipher("RSA");

        System.out.println("Diplomado - Ago 20/202");
        System.out.println("Echo client is running ... connecting the server in "+ this.server + ":" + this.port);
        System.out.println("Other usage: EchoClient host port. Ex: EchoClient localhost 3600");
    }

    public ClienteMensajeria(String server, int port) {
        this.server = server;
        this.port = port;
        System.out.println("Diplomado - Ago 20/2024");
        System.out.println("Echo client is running ... connecting the server in "+ this.server + ":" + this.port);
    }


    private void createStreams(Socket socket) throws IOException {
        toNetwork = new PrintWriter(socket.getOutputStream(), true);
        fromNetwork = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void protocol(Socket socket) throws Exception {
            createStreams(socket);
            System.out.print("Escriba el mensaje separando las acciones con el simbolo de dos puntos (:) :");
            String[] fromUser = SCANNER.nextLine().split(":");
            String action = action(fromUser[0]);
            /**
             * Accion donde se registra un usuario
             * aquí se envia al servidor la llave publica del usuario que se está creando
             */
            if(action.equals("0")){
                toNetwork.println(action);
            }
            if(action.equals("1")) {
                // Se envia la accion que se va a tomar, el nombre del usuario y la llave publica codificada Base64
                toNetwork.println(action + "*" + fromUser[1] + "*" + util.Base64.encode(Util.objectToByteArray(publicKey)));
            }
            if(action.equals("3")){
                toNetwork.println(action + "*" + fromUser[1] );


            }
            // Se envia la peticion para traer la llave del usuario y mandar el mensaje

            //Aquí se pide al servidor consultar la llave publica del usuario determinado
            if(action.equals("2")) {
                toNetwork.println(action + "*" + fromUser[1] );
            }
            //Se solicita al servidor que se va a leer los mensajes de determinado usuario
            if(action.equals("4")) {
                toNetwork.println(action + "*" + fromUser[1] );
            }
            String fromServer[] = fromNetwork.readLine().split("\\*");

            if(fromServer[0].equals("0")){
                System.out.println("[Client] From server: " + fromServer[1]);
                System.out.println("[Client] Finished.");
            }
            // Se recibe la respuesta de la creación del usuario
            if(fromServer[0].equals("1")) {
                System.out.println("[Client] From server: " + fromServer[1]);
                System.out.println("[Client] Finished.");
            }
            // Se recibe la llave del usuario que solicito obtener la llave
            else if (fromServer[0].equals("2")) {
                System.out.println("[Client] From server: "+fromServer[1]);

            }
            //Se recibe la confirmacion del server que se ha enviado un msm
            else if (fromServer[0].equals("3")) {
                if(fromServer.length>2) {
                    System.out.println("[Client] Se ha añadido el mensaje encriptado al buzón de: "+fromServer[1]);
                    PublicKey pk1 = (PublicKey) Util.byteArrayToObject(util.Base64.decode(fromServer[2]));
                    byte encripMsm[] = cipher.encryptMessage(fromUser[2], pk1);
                    toNetwork.println("30*" + fromServer[1] + "*" + util.Base64.encode(encripMsm));
                }else{
                    System.out.println(fromServer[1]);
                }
            }
            //Se recibe el mensaje encriptado del servidor y se desencripta con la llave privada
            else if (fromServer[0].equals("4")) {
                //byte[] mensajeClar= util.Base64.decode(fromServer[0]);
                if(fromServer.length>3) {

                    String[] mensajesFromServer=fromServer[3].split(" ");
                    System.out.println();
                    for (int i = 0; i < mensajesFromServer.length; i++) {
                        System.out.println("[Client] mensaje #"+i+" from server: "+mensajesFromServer[i]+" de: "+mensajesFromServer.length);
                        try {
                            String textClaro= cipher.decryptMessage(util.Base64.decode(mensajesFromServer[i]), privateKey);
                            System.out.println(textClaro);
                        } catch (Exception e) {
                            System.out.println("No puede leer mensajes, no corresponde el cliente.");
                        }
                    }


                }else {
                    System.out.println("[Client] from server: "+fromServer[1]);
                }

            } else if (fromServer[0].equals("5")) {
                PublicKey pk1=(PublicKey) Util.byteArrayToObject(util.Base64.decode(fromServer[4]));
                //PublicKey pk1=(PublicKey) Util.byteArrayToObject(util.Base64.decode(llavePublicaMensaje));
                byte[] mensajeEncriptado=cipher.encryptMessage(fromUser[2], pk1);
                System.out.println("Enviando mensaje encriptado: "+mensajeEncriptado.toString());
                toNetwork.println(action + "*" + fromUser[1] + "*" + util.Base64.encode(mensajeEncriptado));
            } else if (fromServer[0].equals("30")) {
                System.out.println("recibiendo desde server"+fromServer[1]);
            }
        init();

    }

    public String action(String action){
        switch(action) {
            case "REGISTRAR":return "1";
            case "OBTENER_LLAVE_PUBLICA":return "2";
            case "ENVIAR":return "3";
            case "LEER":return "4";
            default: return "0";
        }


    }
    public void init() throws Exception {
        clientSideSocket = new Socket(this.server, this.port);

        protocol(clientSideSocket);

        clientSideSocket.close();
    }

    public static void main(String args[]) throws Exception {
        ClienteMensajeria ec = null;
        if (args.length == 0) {
            ec = new ClienteMensajeria();

        } else {
            String server = args[0];
            int port = Integer.parseInt(args[1]);
            ec = new ClienteMensajeria(server, port);
        }
        ec.init();
    }
}
