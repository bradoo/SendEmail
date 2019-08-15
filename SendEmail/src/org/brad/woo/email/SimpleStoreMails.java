package org.brad.woo.email;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;  
import java.util.Properties;  
 
import javax.mail.Folder;  
import javax.mail.Message;  
import javax.mail.Session;  
import javax.mail.Store;  
 
/**  
 * 简单的邮件接收程序，打印出邮件的原始内容  
 * @author haolloyin  
 */ 
public class SimpleStoreMails {  
    public static void main(String[] args) throws Exception {
    	Properties config = new Properties();
		config.load(new FileInputStream(new File("config/config.properties")));
          
        // 创建一个有具体连接信息的Properties对象  
        Properties props = new Properties();  
        props.setProperty("mail.store.protocol", "pop3");  
        props.setProperty("mail.pop3.host", config.getProperty("mail.pop3.host"));  
          
        // 使用Properties对象获得Session对象  
        Session session = Session.getInstance(props);  
        session.setDebug(true);  
          
        // 利用Session对象获得Store对象，并连接pop3服务器  
        Store store = session.getStore();  
        store.connect(config.getProperty("mail.pop3.host"), config.getProperty("mail.user"), config.getProperty("mail.pwd"));  
          
        // 获得邮箱内的邮件夹Folder对象，以"只读"打开  
        Folder folder = store.getFolder("inbox");  
        folder.open(Folder.READ_ONLY);  
          
        // 获得邮件夹Folder内的所有邮件Message对象  
        Message [] messages = folder.getMessages();  
          
        int mailCounts = messages.length;  
        
        System.out.println("There are totally "+mailCounts+" mails in the inbox");
        
        for(int i = 0; i < mailCounts; i++) {  
              
            String subject = messages[i].getSubject();  
            String from = (messages[i].getFrom()[0]).toString();  
              
            System.out.println("第 " + (i+1) + "封邮件的主题：" + subject);  
            System.out.println("第 " + (i+1) + "封邮件的发件人地址：" + from);  
              
            System.out.println("是否打开该邮件(yes/no)?：");  
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
            String input = br.readLine();  
            if("yes".equalsIgnoreCase(input)) {  
                // 直接输出到控制台中  
                messages[i].writeTo(System.out);  
            }             
        }  
        folder.close(false);  
        store.close();  
    }  
} 