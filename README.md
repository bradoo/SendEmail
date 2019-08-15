# SendEmail
The program was developed to send group emails for all the participants of Dalian Toastmaster Conference in 2019.

As the designer and developer, I want to share the program for more people and organizations.

You only need 3 steps to use this program to send emails:

Step 1: update your email information in the config/config.propertis file
mail.smtp.host=smtp.xxx.com
mail.pop3.host=pop3.xxxx.com
mail.sender=xxxxx
mail.smtp.auth=true
mail.user=xxxxx@xxxx.com
mail.pwd=xxxxx
mail.subject=\u5927\u8FDE\u5CF0\u4F1A\u95E8\u7968\u786E\u8BA4\u51FD|_Dalian_Conference_Ticket_Confirmation
mail.content=config/mail.html
mail.to=config/buyerstest.csv

Step 2: add the email receiver information in the file buyerstest.csv
订单编号,姓名,Name,手机号码,Email,俱乐部,中区,门票类型,
00000000,张三,Zhangsan,12345678900,zhangshan@lisi.com,Baijiaxin,E,早鸟票

Step 3: Execute the org.brad.woo.email.SendEmail to sent the email
$java org.brad.woo.email.SendEmail

Developer: Brad
on 2019.8.15
