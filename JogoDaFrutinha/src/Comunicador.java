import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 *
 * @author Cássio Gamarra & Frederico Hartmann
 */
public class Comunicador extends ReceiverAdapter {
    //Define o JChannel
    JChannel channel;
    Chat chat;
    
    public void entrar(Chat chat){
        
            this.chat = chat;
            //Seta a preferencia por IPV4
            System.setProperty("java.net.preferIPv4Stack", "true");
            //Verifica se o apelido foi preenchido
            if(this.chat.getFieldApelido().getText().equals("")){
                JOptionPane.showMessageDialog(null, "Digite seu apelido...");
            }
            else{
                try {
                    channel = new JChannel(); //Cria um novo channel
                    channel.setReceiver(this); //Define a classe que implementa o método de callback
                    channel.setName(this.chat.getFieldApelido().getText()); //Seta o nome com o apelido digitado
                    channel.connect("Chat - JGroups"); //Define o nome do grupo
                    //Desativa e ativa botoes e campos
                    this.chat.getFieldApelido().setEnabled(false);
                    this.chat.getBtnEntrar().setEnabled(false);
                    this.chat.getBtnSair().setEnabled(true);
                }
                catch (Exception ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //Envia uma mensagem publica
        public void falar(String mensagem){
            if(!mensagem.equals("")){
                Message msg = new Message(null, mensagem);
                try {
                    channel.send(msg);
                }
                catch (Exception ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //Envia uma mensagem privada
        public void sussurar(String destinatario, String mensagem){
            View view = channel.getView(); //Instancia a view do canal
            List<Address> membros = view.getMembers(); //Cria uma lista com todos os membros

            //Pegar o destinatario, ver se o destinatario é mesmo na lista de membro, dai mandar msg
            membros.stream().filter(membro -> (destinatario.equals(membro.toString()))).forEach(
                membro -> {
                    try {
                        Message msg = new Message(membro, mensagem);
                        channel.send(msg);
                    } catch (Exception ex) {
                        Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            );
        }
        @Override
        public void receive(Message msg) {
            String msgChat = msg.getSrc().toString();
            //verifica se a mensagem possui um destinatario, ou seja, é privada
            if(msg.getDest() != null){
                msgChat += " sussurou para você: "+msg.getObject()+"\n";
            }
            //Envia para todos no chat
            else{
                msgChat += msg.getObject()+"\n";
            }
        }
        
        //Callback para atualizar as entradas e saídas do grupo
        @Override
        public void viewAccepted(View view_atual) {
            String info;
            List<Address> membros = view_atual.getMembers();
            
            info = "";
            //Atualiza o texto de informações
            info = "---PONTUAÇÃO---\n";
            info += "\nHost: "+view_atual.getCreator();
            info += "\n---------------\n";
            //Itera sobre os membros
            for (Address membro : membros){
                info += membro+"\n";
            }
            this.chat.getjTextInfo().setText(info);
        }
}
