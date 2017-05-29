import java.awt.*;
import java.awt.event.*;

public class StringRequest extends Dialog {
  BorderLayout borderLayout1 = new BorderLayout();
  Label label1 = new Label();
  Panel panel1 = new Panel();
  Panel panel2 = new Panel();
  Button button1 = new Button();
  BorderLayout borderLayout2 = new BorderLayout();
  Button button2 = new Button();
  TextField textField1 = null;
  String result = null;

  public StringRequest(Frame parent, String msg, String def, boolean question) {
    super(parent, true);
    try {
      this.setSize(new Dimension(209, 114));
      this.setTitle("Aardappel Request:");
      setBackground(SystemColor.control);
      label1.setText(msg);
      panel2.setLayout(borderLayout2);
      button1.setLabel("Ok");
      button2.setLabel("Cancel");
      this.setLayout(borderLayout1);
      this.add(label1, BorderLayout.NORTH);
      this.add(panel1, BorderLayout.SOUTH);
      this.add(panel2, BorderLayout.SOUTH);
      panel2.add(button1, BorderLayout.WEST);
      button1.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              quit(1);
            }
          });
      if (question) panel2.add(button2, BorderLayout.EAST);
      button2.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              quit(0);
            }
          });
      this.addWindowListener(
          new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              quit(0);
            }
          });
      if (def != null) {
        textField1 = new TextField(def);
        this.add(textField1, BorderLayout.CENTER);
        textField1.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                quit(1);
              }
            });
      }
      this.pack();
      this.setLocation(200, 200);
      this.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void quit(int n) {
    if (n == 1) result = (textField1 == null) ? "" : textField1.getText();
    this.dispose();
  }
}
