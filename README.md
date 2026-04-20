# Deber 3 - UDP

Deber utilizando sockets UDP para realizar un cuestionario sobre Desarrollo de Software.

El sistema tiene dos partes:

- `ServidorUDP`: genera las preguntas, recibe las respuestas de los clientes y valida si son correctas o incorrectas.
- `ClienteUDP`: muestra una interfaz grafica en JavaFX para responder el cuestionario.

## Funcionamiento

El servidor se ejecuta en el puerto `5000`.  
El cliente se conecta al servidor usando `localhost` y envia sus respuestas mediante UDP.

El servidor atiende las peticiones usando hilos, de forma que cada mensaje recibido se procesa de manera independiente.

## Archivos principales

- `src/ServidorUDP.java`
- `src/ClienteUDP.java`

## Ejecucion

Primero ejecutar:

```bash
ServidorUDP
```

Luego ejecutar:

```bash
ClienteUDP
```

Para ejecutar el cliente es necesario tener JavaFX configurado en el proyecto.
