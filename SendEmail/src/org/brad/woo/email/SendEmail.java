package org.brad.woo.email;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
public class SendEmail extends Thread {
	static Properties config;
	static Properties properties;
	static Authenticator auth;
	String buyer;
	static volatile int counter = 0;
	static volatile int counterFailed = 0;
	static ExecutorService execSvc = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	static volatile BufferedWriter failedList;
	static volatile BufferedWriter succeededList;
	public static void main(String[] args) {
		long timestamp=System.currentTimeMillis();
		BufferedReader br=null;
		try {
			File logFolder=new File("logs");
			if(!logFolder.exists())
				logFolder.mkdirs();
			File systemOutFile=new File("logs/SystemOut_"+timestamp+".log");
			System.out.println("Please check the debug log on file:"+systemOutFile.getAbsolutePath());
			File systemErrFile=new File("logs/SystemErr_"+timestamp+".log");
			System.out.println("Please check the debug log on file:"+systemErrFile.getAbsolutePath());
			System.setOut(new PrintStream(systemOutFile));
			System.setErr(new PrintStream(systemErrFile));
			failedList=new BufferedWriter(new FileWriter("config/failedBuyers"+timestamp+".csv",true));
			succeededList=new BufferedWriter(new FileWriter("config/succeededBuyers"+timestamp+".csv",true));
			config = new Properties();
			config.load(new FileInputStream(new File("config/config.properties")));
			properties = System.getProperties();

			// Setting up mail server
			properties.setProperty("mail.smtp.host", config.getProperty("mail.smtp.host"));
			properties.setProperty("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
			auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(config.getProperty("mail.user"), config.getProperty("mail.pwd"));
				}
			};
			br = new BufferedReader(new FileReader(new File(config.getProperty("mail.to"))));
			String buyer = br.readLine();
//			System.out.println(buyer);
			succeededList.write(buyer);
			succeededList.newLine();
			succeededList.flush();
			failedList.write(buyer);
			failedList.newLine();
			failedList.flush();
			do {
				buyer = br.readLine();
				if (buyer != null) {
					SendEmail se = new SendEmail(buyer);
					execSvc.execute(se);
				}
			} while (buyer != null);
			while(!execSvc.isTerminated()){
				  Thread.sleep(5000);
				  execSvc.shutdown();
			}
			System.out.println(
					"In this task, totally " + counter + " emails are sent successfully and "+counterFailed+" emails failed to send.");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		}
	}
	static boolean allcompleted = false;
	public SendEmail(String buyer) {
		this.buyer = buyer;
	}
	public void run() {
		try {
			String[] buyerInfo = buyer.replaceAll("\"", "").split(",");
			Session session = Session.getDefaultInstance(properties, auth);
			MimeMessage message = new MimeMessage(session);
			MimeMultipart multipart = new MimeMultipart();
			StringBuilder sb = new StringBuilder();
			message.setFrom(new InternetAddress(config.getProperty("mail.sender")));
			message.addRecipients(Message.RecipientType.TO, buyerInfo[4]);
			message.setSubject(config.getProperty("mail.subject"));
			BufferedReader br = new BufferedReader(new FileReader(new File(config.getProperty("mail.content"))));
			String str = null;
			do {
				str = br.readLine();
				if (str != null)
					sb.append(str);
			} while (str != null);
			br.close();
			String replacedEmail = sb.toString().replaceAll("#orderid#", buyerInfo[0])
					.replaceAll("#cn_name#", buyerInfo[1]).replaceAll("#en_name#", buyerInfo[2])
					.replaceAll("#email#", buyerInfo[4].trim()).replaceAll("#mobile#", buyerInfo[3])
					.replaceAll("#club#", buyerInfo[5]).replaceAll("#division#", buyerInfo[6])
					.replaceAll("#area#", buyerInfo[7]);
//
//			if(buyerInfo[3].endsWith("@yeah.net")){
//				message.setText(replacedEmail);
//			}
			this.addHtml(multipart, replacedEmail);
			
			if (buyerInfo[7].equals("早鸟票"))
				this.addImage(multipart, new File("attachments/earlybird.jpg"));
			if (buyerInfo[7].equals("优惠票"))
				this.addImage(multipart, new File("attachments/youhui.jpg"));
			if (buyerInfo[7].equals("团购票"))
				this.addImage(multipart, new File("attachments/tuangou.jpg"));
			if (buyerInfo[7].equals("正价票"))
				this.addImage(multipart, new File("attachments/zhengjia.jpg"));
			this.addAttach(multipart, new File("attachments/DalianConference.jpg"));
			this.addHtml(multipart, "<body></html>");
			message.setContent(multipart);
			Transport.send(message);
			counter++;
//			System.out.println((counter) + ".Mail successfully sent to : " + buyerInfo[3]);
			synchronized(succeededList){
				succeededList.write(buyer);
				succeededList.newLine();
				succeededList.flush();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			counterFailed++;
//			System.err.println((counterFailed) + ".Mail failed to send to : " + buyer);
			try {
				synchronized(failedList){
					failedList.write(buyer);
					failedList.newLine();
					failedList.flush();
				}
			} catch (IOException e1) {
				System.err.println(e1.getMessage());
			}
		}
	}
	private void addAttach(MimeMultipart multipart, File attach, String header) throws MessagingException {
		BodyPart bodyPart = new MimeBodyPart();
		DataSource dataSource = new FileDataSource(attach);
		bodyPart.setDataHandler(new DataHandler(dataSource));
		bodyPart.setFileName(attach.getName());
		if (header != null) {
			bodyPart.setHeader("Content-ID", "<" + header + ">");
		}
		multipart.addBodyPart(bodyPart);
	}
	/**
	 * 在邮件内容中增加附件（邮件中单独添加附件时使用）
	 * 
	 * @param attach
	 *            File 附件
	 * @throws MessagingException
	 */
	public void addAttach(MimeMultipart multipart, File attach) throws MessagingException {
		addAttach(multipart, attach, null);
	}
	/**
	 * 在邮件中添加 html 代码
	 * 
	 * @param html
	 *            String
	 * @throws MessagingException
	 */
	public void addHtml(MimeMultipart multipart, String html) throws MessagingException {
		BodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent(html, "text/html;charset=utf8");
		multipart.addBodyPart(bodyPart);
	}
	/**
	 * 在邮件中添加可以显示的图片
	 * 
	 * @param image
	 *            File 图片
	 * @throws MessagingException
	 */
	public void addImage(MimeMultipart multipart, File image) throws MessagingException {
		String header = UUID.randomUUID().toString();
		String img = "<div><img src=\"cid:" + header + "\" width=\"800\" height=\"208\"></div>";
		addHtml(multipart, img);
		addAttach(multipart, image, header);
	}
}