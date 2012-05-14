/*
 * CoNect 4  Copyright (C) 2012  Nihanth Subramanya
 * 
 * CoNect 4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CoNect 4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with CoNect 4.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package conect4;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

class Server implements Runnable {

    protected static int port = 2663;
    private Hashtable idcon = new Hashtable();
    private int id = 0;
    static final String CRLF = "\r\n";
    private Hashtable names = new Hashtable();
    JFrame consoleWindow = new JFrame("CoNecT 4 server console window");
    JTextArea area = new JTextArea();
    JScrollPane pane = new JScrollPane(area);

    synchronized void addConnection(Socket s) {
        new ClientConnection(this, s, id);
        id++;
    }

    synchronized boolean set(String the_id, ClientConnection con) {
        if (names.containsKey(con.getName())) {
            con.write("idtaken " + CRLF);
            return false;
        }
        con.write("success " + CRLF);
        idcon.remove(the_id);
        Enumeration e = idcon.keys();
        while (e.hasMoreElements()) {
            String id_ = (String) e.nextElement();
            ClientConnection other = (ClientConnection) idcon.get(id_);
            con.write("add " + other + CRLF);
        }
        idcon.put(the_id, con);
        names.put(con.getName(), "ok");
        broadcast(the_id, "add " + con);
        return true;
    }

    synchronized void sendto(String dest, String body) {
        ClientConnection con = (ClientConnection) idcon.get(dest);
        if (con != null) {
            con.write(body + CRLF);
        }
    }

    synchronized void broadcast(String exclude, String body) {
        Enumeration e = idcon.keys();
        while (e.hasMoreElements()) {
            String id_ = (String) e.nextElement();
            if (!exclude.equals(id_)) {
                ClientConnection con = (ClientConnection) idcon.get(id_);
                con.write(body + CRLF);
            }
        }
    }

    synchronized void delete(String the_id) {
        broadcast(the_id, "delete " + the_id);
    }

    synchronized void kill(ClientConnection c) {
        if (idcon.remove(c.getId()) == c) {
            delete(c.getId());
            names.remove(c.getName());
        }
    }

    void printToConsole(String text) {
        area.append(text + CRLF);
    }

    @Override
    public void run() {
        try {
            try {
                consoleWindow.setLayout(new BorderLayout());
                area.setBackground(Color.black);
                area.setForeground(Color.white);
                area.setFont(new Font("Courier", Font.BOLD, 15));
                consoleWindow.add(pane, BorderLayout.CENTER);
                consoleWindow.pack();
                consoleWindow.setSize(480, 360);
                area.setEditable(false);
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();
                    TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/icon.png")));
                    PopupMenu menu = new PopupMenu();
                    final MenuItem one = new MenuItem("Show console...");
                    final MenuItem two = new MenuItem("Stop server...");
                    ActionListener listener = new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            Object src = ae.getSource();
                            if (src == one) {
                                consoleWindow.setVisible(true);
                            } else if (src == two) {
                                System.exit(0);
                            }
                        }
                    };
                    one.addActionListener(listener);
                    two.addActionListener(listener);
                    menu.add(one);
                    menu.add(two);
                    icon.setPopupMenu(menu);
                    tray.add(icon);
                }
            } catch (AWTException ex) {
                printToConsole("Unable to initialize tray icon");
            }
            ServerSocket acceptSocket = new ServerSocket(port);
            printToConsole("Server listening on port " + port);
            while (true) {
                Socket s = acceptSocket.accept();
                addConnection(s);
            }
        } catch (IOException e) {
            printToConsole("accept loop IOException: " + e);
        }
    }

    public static void main(String[] args) {
        new Thread(new Server()).start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        }
    }
}

class ClientConnection implements Runnable {

    private Socket sock;
    private BufferedReader in;
    private OutputStream out;
    private String host;
    private Server server;
    private static final String CRLF = "\r\n";
    private String name = null;
    private String id;
    static private final int NAME = 1;
    static private final int DELETE = 2;
    static private final int SEND = 3;
    static private final int QUIT = 4;
    static private Hashtable keys = new Hashtable();


    static {
        keys.put("name", NAME);
        keys.put("delete", DELETE);
        keys.put("send", SEND);
        keys.put("quit", QUIT);
    }

    public ClientConnection(Server srv, Socket s, int i) {
        try {
            server = srv;
            sock = s;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = s.getOutputStream();
            host = s.getInetAddress().getHostName();
            id = "" + i;
            write("id " + id + CRLF);
            new Thread(this).start();
        } catch (IOException e) {
            server.printToConsole("failed ClientConnection " + e);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id + " " + host + " " + name;
    }

    public String getHost() {
        return host;
    }

    public String getId() {
        return id;
    }

    public void close() {
        server.kill(this);
        try {
            sock.close();
        } catch (IOException e) {
        }
    }

    public void write(String string) {
        byte buf[] = string.getBytes();
        try {
            out.write(buf, 0, buf.length);
        } catch (IOException e) {
            close();
        }
    }

    public String readline() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    private int lookup(String s) {
        Integer i = (Integer) keys.get(s);
        return i == null ? -1 : i;
    }

    @Override
    public void run() {
        String s;
        StringTokenizer st;
        while ((s = readline()) != null) {
            st = new StringTokenizer(s);
            String keyword = st.nextToken();
            switch (lookup(keyword)) {
                case NAME: {
                    name = st.nextToken() + (st.hasMoreTokens() ? " " + st.nextToken(CRLF) : "");
                    if (server.set(id, this)) {
                        server.printToConsole(" [" + new Date() + "] " + this);
                    }
                    break;
                }
                case DELETE: {
                    server.delete(id);
                    server.printToConsole("delete " + id);
                    break;
                }
                case SEND: {
                    String body = st.nextToken(CRLF);
                    server.broadcast("", body);
                    server.printToConsole(body);
                    break;
                }
                case QUIT: {
                    close();
                    return;
                }
            }
        }
        close();
    }
}

class ServerConnection implements Runnable {

    private RackView view;
    private static final String CRLF = "\r\n";
    private BufferedReader in;
    private PrintWriter out;
    private String id = "";
    private Thread t;
    private static final int ID = 1;
    private static final int ADD = 2;
    private static final int DELETE = 3;
    private static final int CHAT = 4;
    private static final int IDTAKEN = 5;
    private static final int SUCCESS = 6;
    private static final int PLAY = 7;
    private static final int START = 8;
    private static final int CANT = 9;
    private static final int PLAYABLE = 10;
    private static final int TAKEN = 11;
    private static final int READD = 12;
    private static Hashtable keys = new Hashtable();
    private boolean playing = false;


    static {
        keys.put("id", ID);
        keys.put("add", ADD);
        keys.put("delete", DELETE);
        keys.put("chat", CHAT);
        keys.put("idtaken", IDTAKEN);
        keys.put("success", SUCCESS);
        keys.put("play", PLAY);
        keys.put("start", START);
        keys.put("cant", CANT);
        keys.put("playable", PLAYABLE);
        keys.put("taken", TAKEN);
        keys.put("readd", READD);
    }
    private static Hashtable idcon = new Hashtable();
    private int playablecount = 0;

    public ServerConnection(RackView view, String site) throws IOException {
        this.view = view;
        Socket server = new Socket(site, Server.port);
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        out = new PrintWriter(server.getOutputStream(), true);
        start();
    }

    private String readline() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    boolean isPlaying() {
        return playing;
    }

    void gameEnded() {
        send(view.theirName() + " delete " + view.getClientName());
        send("all" + " readd " + view.getClientName());
    }

    void setName(String s) {
        out.println("name " + s);
    }

    void delete() {
        send("all" + " delete " + view.getClientName());
        out.println("delete " + view.getClientName());
        out.println("quit");
    }

    void send(String s) {
        out.println("send " + s);
    }

    void chat(String s) {
        send(view.theirName() + " chat " + s);
    }

    void startGame(String to) {
        send("all" + " start " + view.getClientName() + " " + to);
    }

    String getID() {
        return String.valueOf(Integer.parseInt(id) + 1);
    }

    void play(int col) {
        send(view.theirName() + " play " + col);
    }

    void start() {
        t = new Thread(this);
        t.start();
    }

    void playable(boolean b) {
        send(view.theirName() + " playable " + b);
        send(view.getClientName() + " playable " + b);
    }

    private int lookup(String s) {
        Integer i = (Integer) keys.get(s);
        return i == null ? -1 : i.intValue();
    }

    @Override
    public void run() {
        String s;
        StringTokenizer st;
        while ((s = readline()) != null) {
            st = new StringTokenizer(s);
            String keyword = st.nextToken();
            if (lookup(keyword) == -1 && !keyword.equals(view.getClientName()) && !keyword.equals("all")) {
                continue;
            }
            if (lookup(keyword) == -1) keyword = st.nextToken();
            switch (lookup(keyword)) {
                case ID:
                    id = st.nextToken();
                    break;
                case ADD:
                    String id_ = st.nextToken();
                    String host = st.nextToken();
                    String name = st.nextToken();
                    idcon.put(id_, name);
                    view.add(name);
                    break;
                case DELETE:
                    String who = st.nextToken();
                    view.delete((String) idcon.get(who));
                    if (who.equals(view.theirName())) view.otherQuit(who);
                    break;
                case CHAT:
                    String from = st.nextToken();
                    view.chat(from, st.nextToken(CRLF));
                    break;
                case IDTAKEN:
                    view.idtaken();
                    break;
                case SUCCESS:
                    view.success();
                    break;
                case PLAY:
                    view.lanPlay(Integer.parseInt(st.nextToken()));
                    break;
                case START:
                    if (view.startGame(st.nextToken(), st.nextToken())) {
                        send("all" + " taken " + view.getClientName());
                    } else {
                        break;
                    }
                    break;
                case CANT:
                    view.cant(st.nextToken(), st.nextToken());
                    break;
                case PLAYABLE:
                    boolean b = Boolean.parseBoolean(st.nextToken());
                    if (!b) {
                        view.playable = false;
                        playablecount = 0;
                    } else {
                        playablecount++;
                        if (playablecount == 2) {
                            view.playable = b;
                            playablecount = 0;
                        }
                    }
                    break;
                case TAKEN:
                    view.delete(st.nextToken());
                    break;
                case READD:
                    view.add(st.nextToken());
                    break;
            }
        }
    }
}