package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import database.*;

public class ServerThread extends Thread {
	private Socket socket = null;
	private String id = null;
	List<PrintWriter> listWriters = null;
	
	public ServerThread(Socket socket, List<PrintWriter> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
	}
	
	@Override
	public void run() {
		try {
		    BufferedReader buffereedReader =
		            new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

		    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

		    while(true) {
		        String request = buffereedReader.readLine();

		        if( request == null) {
		            System.out.println("Lost connection from client");
		            doQuit(printWriter);
		            break;
		        }
		        
		        String[] tokens = request.split(":");
		        if("LogIn".equals(tokens[0])) {
                	String pass = DBMembers.members_load(tokens[1]);
                	
                	printWriter.println(pass);
                	printWriter.flush();
                }
                else if("MLogIn".equals(tokens[0])) {
                	String pass = DBManagers.managers_load(tokens[1]);
                	
                	printWriter.println(pass);
                	printWriter.flush();
                }
                
                else if("Register".equals(tokens[0])) {
                	DBMembers.members_insert(tokens[1], tokens[2], tokens[3], tokens[4]);
                }
                
                else if("checkID".equals(tokens[0])) {
                	String check = String.valueOf(DBMembers.IDcheck(tokens[1]));
                	
                	printWriter.println(check);
                	printWriter.flush();
                }
                else if("addItem".equals(tokens[0])) {
                	DBItems.add(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
                }
                
                else if("myInfo".equals(tokens[0])) {
                	String InfoList = DBMembers.load_myInfo(tokens[1]);
                	printWriter.println(InfoList);
                	printWriter.flush();
                }
                
                else if("loadOnePost".equals(tokens[0])) {
                	String result = DBItems.loadOnePost(tokens[1]);
                	printWriter.println(result);
                	printWriter.flush();
                }
                else if("itemsCnt".equals(tokens[0])) {
                	String cnt = DBItems.itemsCount();
                	printWriter.println(cnt);
                	printWriter.flush();
                }
                else if("MyThings".equals(tokens[0])) {
                	String ThingsList = DBThings.loadMyThings(tokens[1]);
                	printWriter.println(ThingsList);
                	printWriter.flush();
                }
                else if("YourThings".equals(tokens[0])) {
                	String ThingsList = DBThings.loadYourThings(tokens[1]);
                	printWriter.println(ThingsList);
                	printWriter.flush();;
                }
                
                else if("loadItemList".equals(tokens[0])) {
                	String Items = DBItems.loadItemList();
                	printWriter.println(Items);
                	printWriter.flush();
                }
		        
                else if("deleteData".equals(tokens[0])) {
                	DBManagers.deleteData(tokens[1]);
                }
                else if("rent".equals(tokens[0])) {
                	DBrent.rentItem(tokens[1], tokens[2]);
                	
                	DBrent.addNotice(tokens[3], tokens[2], tokens[1], "대여");
                	
                }
                else if("rentSt".equals(tokens[0])) {
                	String rs = DBrent.loadRentState(tokens[1]);
                	
                	printWriter.println(rs);
                	printWriter.flush();
                }
                else if("loadNoti".equals(tokens[0])) {
                	String rs = DBrent.loadNotice(tokens[1]);
                	
                	printWriter.println(rs);
                	printWriter.flush();
                }
                else if("returnItem".equals(tokens[0])) {
                	DBrent.returnItem(tokens[1]);
                }
		        
                else if("loadCat".equals(tokens[0])) {
                	String list = DBItems.loadCategory(tokens[1]);
                	
                	printWriter.println(list);
                	printWriter.flush();
                }

		    }
		}
			catch(IOException e) {
				System.out.println(this.id + "exit from server");
		}
	}
	
    private void removeWriter(PrintWriter writer) {
        synchronized (listWriters) {
            listWriters.remove(writer);
        }
    }
	
    private void doQuit(PrintWriter writer) {
        removeWriter(writer);

        String data = this.id + "End Connection";
        broadcast(data);
    }
    
    private void broadcast(String data) {
        synchronized (listWriters) {
            for(PrintWriter writer : listWriters) {
                writer.println(data);
                writer.flush();
            }
        }
    }
}