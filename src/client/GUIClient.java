/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import util.Config;

/**
 *
 * @author chandra
 */
public class GUIClient extends javax.swing.JFrame {

    private Client client;
    
    /**
     * Creates new form GUIClient
     */
    public GUIClient(Client client) {
        this.client = client;
        initComponents();
        panelWelcome.setVisible(true);
        panelMain.setVisible(false);
        
        buttonStopLeavingAMessage.setEnabled(false);
    }

    public void SetVoiceMailClient(Client client){
        this.client = client;
    }
    
    public javax.swing.JList getListMessagesList(){
        return listMessagesList;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelWelcome = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textFieldMyName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        buttonSignIn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        textFieldServerIP = new javax.swing.JTextField();
        panelMain = new javax.swing.JPanel();
        labelMainMessage = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listMessagesList = new javax.swing.JList();
        buttonListenMessage = new javax.swing.JButton();
        buttonDeleteMessage = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        textFieldToName = new javax.swing.JTextField();
        buttonLeaveAMessage = new javax.swing.JButton();
        buttonStopLeavingAMessage = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Hi, please input your name");

        jLabel2.setText("VoiceMail App");

        buttonSignIn.setText("sign in");
        buttonSignIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSignInActionPerformed(evt);
            }
        });

        jLabel3.setText("Server IP:");

        javax.swing.GroupLayout panelWelcomeLayout = new javax.swing.GroupLayout(panelWelcome);
        panelWelcome.setLayout(panelWelcomeLayout);
        panelWelcomeLayout.setHorizontalGroup(
            panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWelcomeLayout.createSequentialGroup()
                .addGroup(panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelWelcomeLayout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addGroup(panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelWelcomeLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(45, 45, 45))))
                    .addGroup(panelWelcomeLayout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(textFieldMyName, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonSignIn))
                    .addGroup(panelWelcomeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(96, Short.MAX_VALUE))
        );
        panelWelcomeLayout.setVerticalGroup(
            panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWelcomeLayout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldMyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSignIn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addGroup(panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textFieldServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );

        labelMainMessage.setText("Hi, these are your voice messages: ");

        jScrollPane1.setViewportView(listMessagesList);

        buttonListenMessage.setText("listen");
        buttonListenMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonListenMessageActionPerformed(evt);
            }
        });

        buttonDeleteMessage.setText("delete");
        buttonDeleteMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteMessageActionPerformed(evt);
            }
        });

        jLabel4.setText("Wanna leave a message?");

        jLabel5.setText("To");

        buttonLeaveAMessage.setText("leave a Message");
        buttonLeaveAMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLeaveAMessageActionPerformed(evt);
            }
        });

        buttonStopLeavingAMessage.setText("stop");
        buttonStopLeavingAMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopLeavingAMessageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(labelMainMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 231, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(18, 18, 18)
                                        .addComponent(textFieldToName))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(17, 17, 17))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(buttonLeaveAMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(89, 89, 89)))
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonListenMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonDeleteMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                            .addComponent(buttonStopLeavingAMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(53, 53, 53))))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelMainMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(buttonListenMessage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonDeleteMessage)))
                .addGap(26, 26, 26)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(textFieldToName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonStopLeavingAMessage)
                    .addComponent(buttonLeaveAMessage))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelWelcome, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelWelcome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSignInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSignInActionPerformed
        panelWelcome.setVisible(false);
        panelMain.setVisible(true);
        System.out.println("myname: "+ textFieldMyName.getText());
        client.signIn(textFieldMyName.getText());
        if (!textFieldServerIP.getText().equalsIgnoreCase("")){
            Config.serverAddress = textFieldServerIP.getText();
        }
    }//GEN-LAST:event_buttonSignInActionPerformed

    private void buttonLeaveAMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLeaveAMessageActionPerformed
        client.leaveAMessage(textFieldToName.getText());
    }//GEN-LAST:event_buttonLeaveAMessageActionPerformed

    private void buttonStopLeavingAMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopLeavingAMessageActionPerformed
        client.stopSendingMessage();
    }//GEN-LAST:event_buttonStopLeavingAMessageActionPerformed

    private void buttonListenMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonListenMessageActionPerformed
        int selectedMessage = listMessagesList.getSelectedIndex();
        if (selectedMessage != -1){
           client.listenMessage(selectedMessage);
        }
        
    }//GEN-LAST:event_buttonListenMessageActionPerformed

    private void buttonDeleteMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteMessageActionPerformed
        int selectedMessage = listMessagesList.getSelectedIndex();
        if (selectedMessage != -1){
           client.deleteMessage(selectedMessage);
        }
    }//GEN-LAST:event_buttonDeleteMessageActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteMessage;
    private javax.swing.JButton buttonLeaveAMessage;
    private javax.swing.JButton buttonListenMessage;
    private javax.swing.JButton buttonSignIn;
    private javax.swing.JButton buttonStopLeavingAMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelMainMessage;
    private javax.swing.JList listMessagesList;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelWelcome;
    private javax.swing.JTextField textFieldMyName;
    private javax.swing.JTextField textFieldServerIP;
    private javax.swing.JTextField textFieldToName;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JLabel getLabelMainMessage(){
        return labelMainMessage;
    }
    
    public javax.swing.JButton getButtonLeaveAMessage(){
        return buttonLeaveAMessage;
    }
    
    public javax.swing.JButton getButtonStopLeavingAMessage(){
        return buttonStopLeavingAMessage;
    }
}
