import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteUDP extends Application {
    static final int PUERTO_SERVIDOR = 5000;

    Label lblEstado = new Label("Presione iniciar");
    Label lblPregunta = new Label("");
    RadioButton rbA = new RadioButton();
    RadioButton rbB = new RadioButton();
    RadioButton rbC = new RadioButton();
    Button btnIniciar = new Button("Iniciar");
    Button btnResponder = new Button("Responder");
    ToggleGroup grupo = new ToggleGroup();

    DatagramSocket socket;
    InetAddress servidor;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        rbA.setToggleGroup(grupo);
        rbB.setToggleGroup(grupo);
        rbC.setToggleGroup(grupo);
        btnResponder.setDisable(true);

        btnIniciar.setOnAction(e -> iniciar());
        btnResponder.setOnAction(e -> responder());

        VBox root = new VBox(10, lblEstado, lblPregunta, rbA, rbB, rbC, btnIniciar, btnResponder);
        root.setPadding(new Insets(20));

        stage.setTitle("Cliente UDP");
        stage.setScene(new Scene(root, 500, 300));
        stage.show();
    }

    void iniciar() {
        new Thread(() -> {
            try {
                conectarCliente();
                enviar("INICIAR");
                String respuesta = recibir();
                Platform.runLater(() -> mostrarPregunta(respuesta));
            } catch (Exception ex) {
                Platform.runLater(() -> lblEstado.setText("No se pudo conectar al servidor"));
            }
        }).start();
    }

    void conectarCliente() throws Exception {
        servidor = InetAddress.getByName("localhost");
        socket = new DatagramSocket();
    }

    void responder() {
        RadioButton seleccionado = (RadioButton) grupo.getSelectedToggle();
        if (seleccionado == null) {
            lblEstado.setText("Seleccione una respuesta");
            return;
        }

        new Thread(() -> {
            try {
                enviar("RESPUESTA|" + seleccionado.getText().substring(0, 1));
                String respuesta = recibir();
                Platform.runLater(() -> procesarRespuesta(respuesta));
            } catch (Exception ex) {
                Platform.runLater(() -> lblEstado.setText("Error al responder"));
            }
        }).start();
    }

    void procesarRespuesta(String mensaje) {
        String[] partes = mensaje.split("\\|");
        lblEstado.setText(partes[1]);

        if (partes[0].equals("FIN")) {
            lblPregunta.setText(partes[2]);
            rbA.setVisible(false);
            rbB.setVisible(false);
            rbC.setVisible(false);
            btnResponder.setDisable(true);
            btnIniciar.setDisable(false);
        } else {
            mostrarPregunta(partes[2] + "|" + partes[3] + "|" + partes[4] + "|" + partes[5] + "|" + partes[6]);
        }
    }

    void mostrarPregunta(String mensaje) {
        String[] partes = mensaje.split("\\|");
        lblPregunta.setText(partes[1]);
        rbA.setText(partes[2]);
        rbB.setText(partes[3]);
        rbC.setText(partes[4]);
        rbA.setVisible(true);
        rbB.setVisible(true);
        rbC.setVisible(true);
        grupo.selectToggle(null);
        btnIniciar.setDisable(true);
        btnResponder.setDisable(false);
    }

    void enviar(String mensaje) throws Exception {
        byte[] datos = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(datos, datos.length, servidor, PUERTO_SERVIDOR);
        socket.send(paquete);
    }

    String recibir() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        socket.receive(paquete);
        return new String(paquete.getData(), 0, paquete.getLength());
    }
}
