package msm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServidorMensajeria {
    public static final int PORT = 3400;

    private ServerSocket listener;
    private static HashMap<String, String> user;
    private static HashMap<String, ArrayList<String>> buzon;

    private int port;

    public ServidorMensajeria() {
        this.port = PORT;
        System.out.println("Diplomado - Ago 20/2024");
        System.out.println("Echo server is running on port: " + this.port);
    }

    public ServidorMensajeria(int port) {
        this.port = port;
        System.out.println("Echo server is running on port: " + this.port);
    }

    private void init() throws Exception {
        user = new HashMap<>();
        buzon = new HashMap<>();
        listener = new ServerSocket(this.port);

        while (true) {
            // Aceptar la conexi�n del cliente
            Socket serverSideSocket = listener.accept();
            System.out.println("Nuevo cliente conectado: " + serverSideSocket.getInetAddress().getHostAddress());

            // Crear un nuevo hilo para manejar la conexi�n del cliente
            ClientHandler handler = new ClientHandler(serverSideSocket);
            new Thread(handler).start();
        }
    }

    // Clase interna que maneja la conexi�n con cada cliente en un hilo separado
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter toNetwork;
        private BufferedReader fromNetwork;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                createStreams(socket);
                protocol(socket);
            } catch (Exception e) {
                System.out.println("Error manejando al cliente: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error cerrando el socket: " + e.getMessage());
                }
            }
        }

        private void createStreams(Socket socket) throws IOException {
            toNetwork = new PrintWriter(socket.getOutputStream(), true);
            fromNetwork = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        private void protocol(Socket socket) throws Exception {
            createStreams(socket);
            String mensaje[]= fromNetwork.readLine().split("\\*");

            String answer="";
            if(mensaje[0].equals("0")){
                answer="Accion no v�lida";
                toNetwork.println("0*"+answer);
            }
            // Cuando recibe del cliente la solicitud registrar
            if(mensaje[0].equals("1")){
                if(user.get(mensaje[1])!=null){
                    answer = "El usuario "+mensaje[1]+" ya esta registrado ";
                    System.out.println("Cliente no puede registrar usuario existente");
                    toNetwork.println("2*"+answer);
                }else{

                    user.put(mensaje[1],mensaje[2]);
                    answer="Bienvenido "+mensaje[1];//+" ! llave: "+mensaje[2];
                    ArrayList<String> arrayBuzon= new ArrayList<>();
                    buzon.put(mensaje[1],arrayBuzon);
                    System.out.println("Se ha registrado el usuario: "+ mensaje[1] + "\n su llave publica es:"+mensaje[2]);
                    toNetwork.println("1*"+answer);
                }

            }
            //Cuando el usuario solicita la llave de determinado usuario
            else if(mensaje[0].equals("2")){
                if(user.get(mensaje[1])!=null){
                    answer = "Lave p�blica de "+ mensaje[1]+": "+user.get(mensaje[1]);
                    toNetwork.println("2*"+answer);
                }else{
                    user.put(mensaje[1],mensaje[2]);
                    answer="ERROR. El usuario "+mensaje[1]+" no esta registrado";
                    toNetwork.println("2*"+answer);
                }
            }
            //Cuando el cliente solicita llave para cifrar mensaje
            else if (mensaje[0].equals("3")) {
                if (user.get(mensaje[1])!=null) {
                    answer = mensaje[1] + "*" + user.get(mensaje[1]);

                    toNetwork.println("3*" + answer);
                    protocol(socket);
                }else {
                    System.out.println("CLiente tratando de enviar mensaje a usuario no existente");
                    toNetwork.println("3*" + "No se puede entregar mensaje el usuario: "+ mensaje[1]+" no existe.");
                }
            }
            //Cuando el cliente envia mensaje cifrado
            else if (mensaje[0].equals("30")) {
                if(user.get(mensaje[1])!=null){

                    System.out.println("A�adiendo al buz�n de: "+mensaje[1]+" \nel mensaje encriptado: "+ mensaje[2]);
                    buzon.get(mensaje[1]).add(mensaje[2]);
                    System.out.println(buzon+" buzon");
                    System.out.println(buzon.size()+"tama�o del buzon");
                    answer = "Mensaje enviado al usuario: *"+mensaje[1]+"* mensaje: *"+mensaje[2];
                    toNetwork.println("30*"+answer);
                }else{
                    user.put(mensaje[1],mensaje[2]);
                    answer="ERROR. El usuario "+mensaje[1]+" no esta registrado";
                    toNetwork.println("30*"+answer);
                }
            } else if (mensaje[0].equals("4")) {
                if(user.get(mensaje[1])!=null){
                    answer = "Los mensajes del usuario *"+mensaje[1];
                    if(buzon.get(mensaje[1]).isEmpty()){
                        System.out.println("[Server] El usuario: "+ mensaje[1]+ " no tiene mensajes en el buz�n.");
                        answer="No tiene mensajes";
                        toNetwork.println("4*"+answer);
                    }else {
                        String mensajes="";
                        for (int i=0;i< buzon.get(mensaje[1]).size();i++) {
                            mensajes+=buzon.get(mensaje[1]).get(i)+ " ";
                            System.out.println("[Server] Cantidad de mensajes del cliente: "+ buzon.size());

                        }
                        System.out.println("[Server] mensajes para: "+mensaje[1]+": "+mensajes);
                        toNetwork.println("4*" + answer + "*" + mensajes);
                    }
                }else{
                    System.out.println("[Server] Error accediendo a leer mensajes de cliente no existente");
                    answer="ERROR. El usuario "+mensaje[1]+" no esta registrado";
                    toNetwork.println("2*"+answer);
                }

            }

            System.out.println("[Server] Waiting for a new client.");

        }
    }

    public static void main(String[] args) throws Exception {
        ServidorMensajeria servidor;
        if (args.length == 0) {
            servidor = new ServidorMensajeria();
        } else {
            int port = Integer.parseInt(args[0]);
            servidor = new ServidorMensajeria(port);
        }
        servidor.init();
    }
}
