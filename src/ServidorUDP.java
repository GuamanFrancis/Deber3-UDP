import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ServidorUDP {
    static final int PUERTO = 5000;

    static String[][] preguntas = {
            {"Que metodologia usa sprints?", "A) Cascada", "B) Scrum", "C) Big Bang", "B"},
            {"Que herramienta sirve para controlar versiones?", "A) Git", "B) Word", "C) Paint", "A"},
            {"Que significa QA?", "A) Quality Assurance", "B) Quick Access", "C) Query Admin", "A"},
            {"Que prueba revisa una parte pequena del codigo?", "A) Prueba unitaria", "B) Prueba manual", "C) Prueba final", "A"},
            {"Que se hace antes de programar un sistema?", "A) Analizar requisitos", "B) Borrar archivos", "C) Cerrar el IDE", "A"}
    };

    static Map<String, int[]> clientes = new HashMap<>();
    static DatagramSocket socket;

    public static void main(String[] args) {
        try {
            conectarServidor();

            while (true) {
                DatagramPacket paquete = recibirPaquete();

                new Thread(() -> atenderCliente(paquete)).start();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void conectarServidor() throws Exception {
        socket = new DatagramSocket(PUERTO);
        System.out.println("Servidor UDP iniciado en el puerto " + PUERTO);
    }

    static DatagramPacket recibirPaquete() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        socket.receive(paquete);
        return paquete;
    }

    static void atenderCliente(DatagramPacket paquete) {
        try {
            String mensaje = new String(paquete.getData(), 0, paquete.getLength()).trim();
            InetAddress ip = paquete.getAddress();
            int puerto = paquete.getPort();
            String cliente = ip.getHostAddress() + ":" + puerto;

            if (mensaje.equals("INICIAR")) {
                clientes.put(cliente, new int[]{0, 0, 0});
                enviar(crearPregunta(0), ip, puerto);
            } else if (mensaje.startsWith("RESPUESTA|")) {
                revisarRespuesta(cliente, mensaje, ip, puerto);
            }
        } catch (Exception e) {
            System.out.println("Error con cliente: " + e.getMessage());
        }
    }

    static void revisarRespuesta(String cliente, String mensaje, InetAddress ip, int puerto) throws Exception {
        int[] datos = clientes.get(cliente);
        int posicion = datos[0];
        String respuesta = mensaje.split("\\|")[1];
        String correcta = preguntas[posicion][4];

        String resultado;
        if (respuesta.equalsIgnoreCase(correcta)) {
            datos[1]++;
            resultado = "Correcto";
        } else {
            datos[2]++;
            resultado = "Incorrecto. Era " + correcta;
        }

        posicion++;
        datos[0] = posicion;

        if (posicion == preguntas.length) {
            enviar("FIN|" + resultado + "|Correctas: " + datos[1] + " Incorrectas: " + datos[2], ip, puerto);
            clientes.remove(cliente);
        } else {
            enviar("SIGUIENTE|" + resultado + "|" + crearPregunta(posicion), ip, puerto);
        }
    }

    static String crearPregunta(int posicion) {
        return "PREGUNTA|" + preguntas[posicion][0] + "|" + preguntas[posicion][1] + "|"
                + preguntas[posicion][2] + "|" + preguntas[posicion][3];
    }

    static void enviar(String mensaje, InetAddress ip, int puerto) throws Exception {
        byte[] datos = mensaje.getBytes();
        DatagramPacket respuesta = new DatagramPacket(datos, datos.length, ip, puerto);
        socket.send(respuesta);
    }
}
