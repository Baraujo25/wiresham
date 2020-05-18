package us.abstracta.wiresham;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.apple.eawt.Application;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.slf4j.LoggerFactory;

public class VirtualTcpServiceGUI extends JFrame {

  private static final int FIELD_HEIGHT = 25;
  private JTextField flowPath = new JTextField();
  private JTextField portField = new JTextField("23");
  private JCheckBox sslCheck = new JCheckBox();
  private JButton fileChooserButton = new JButton("Choose..");
  private JFileChooser fileChooser = new JFileChooser();
  private JTextField threadField = new JTextField("1");
  private JTextArea console = new JTextArea();
  private JButton startButton = new JButton("START");
  private JButton stopButton = new JButton("STOP");
  private VirtualTcpService service = new VirtualTcpService();

  public VirtualTcpServiceGUI() {
    setLayout(new BorderLayout());
    setSize(480, 480);
    setTitle("Wiresham");
    add(getMainPanel());
    configureComponentsListeners();
    setIcon();
    setResizable(false);
    console.setEditable(false);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void setIcon() {
    Image icon = new ImageIcon("logo.png").getImage();
    if (icon != null) {
      setIconImage(icon);
      Application application = Application.getApplication();
      application.setDockIconImage(icon);

    }
  }

  private void configureComponentsListeners() {
    fileChooserButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        fileChooser.showDialog(VirtualTcpServiceGUI.this, "Load");
      }
    });
    fileChooser.addActionListener(e -> flowPath.setText((fileChooser.getSelectedFile() != null)
        ? fileChooser.getSelectedFile().getAbsolutePath()
        : ""));
    startButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        new SwingWorker() {
          @Override
          protected Object doInBackground() throws IOException {
            start();
            return null;
          }
        }.execute();

      }
    });

    stopButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          service.stop(1000);
        } catch (IOException | InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private void start() throws IOException {
    service.setPort(Integer.valueOf(portField.getText()));
    service.setSslEnabled(sslCheck.isSelected());
    service.setMaxConnections(Integer.valueOf(threadField.getText()));
    service.setFlow(Flow.fromYml(new File(flowPath.getText())));
    service.start();

    try {
      synchronized (this) {
        this.wait();
      }
    } catch (InterruptedException ex) {
      try {
        service.stop(10);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
      Thread.currentThread().interrupt();
    }
  }

  private Component getMainPanel() {
    JLabel flowLabel = new JLabel("Flow: ");
    JPanel configPanel = buildConfigurationPanel();
    JPanel buttonPanel = buildButtonPanel();
    JScrollPane scrollPane = new JScrollPane(console);
    JPanel main = new JPanel();
    GroupLayout layout = new GroupLayout(main);
    main.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
            .addComponent(flowLabel)
            .addComponent(flowPath, GroupLayout.PREFERRED_SIZE,
                250, GroupLayout.PREFERRED_SIZE)
            .addComponent(fileChooserButton))
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(configPanel)
        .addComponent(buttonPanel)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(scrollPane)
    );

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(flowLabel, GroupLayout.Alignment.LEADING)
            .addComponent(flowPath, GroupLayout.PREFERRED_SIZE,
                FIELD_HEIGHT, GroupLayout.PREFERRED_SIZE)
            .addComponent(fileChooserButton, GroupLayout.PREFERRED_SIZE,
                FIELD_HEIGHT, GroupLayout.PREFERRED_SIZE))
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(configPanel)
        .addComponent(buttonPanel)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(scrollPane)
    );
    return main;
  }

  private JPanel buildButtonPanel() {
    JPanel buttonPanel = new JPanel();
    GroupLayout layout = new GroupLayout(buttonPanel);
    buttonPanel.setLayout(layout);
    layout.setAutoCreateGaps(true);

    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(startButton)
        .addGap(GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
        .addComponent(stopButton)
    );

    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
        .addComponent(startButton)
        .addComponent(stopButton)
    );
    return buttonPanel;
  }

  private JPanel buildConfigurationPanel() {
    JPanel configPanel = new JPanel();
    GroupLayout layout = new GroupLayout(configPanel);
    configPanel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    JLabel labelPort = new JLabel("Port: ");
    JLabel sslLabel = new JLabel("SSL: ");
    JLabel threadLabel = new JLabel("Threads: ");

    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(labelPort)
        .addComponent(portField, GroupLayout.PREFERRED_SIZE,
            FIELD_HEIGHT, GroupLayout.PREFERRED_SIZE)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(sslLabel)
        .addComponent(sslCheck, GroupLayout.PREFERRED_SIZE,
            FIELD_HEIGHT, GroupLayout.PREFERRED_SIZE)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(threadLabel)
        .addComponent(threadField, GroupLayout.PREFERRED_SIZE,
            FIELD_HEIGHT, GroupLayout.PREFERRED_SIZE)
    );
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(true, labelPort)
        .addComponent(portField, GroupLayout.PREFERRED_SIZE,
            100, GroupLayout.PREFERRED_SIZE)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(sslLabel)
        .addComponent(sslCheck)
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addComponent(threadLabel)
        .addComponent(threadField, GroupLayout.PREFERRED_SIZE,
            100, GroupLayout.PREFERRED_SIZE)
    );
    return configPanel;
  }

  public JTextArea getConsole() {
    return console;
  }

  public static void main(String[] args) throws InterruptedException {
    LafManager.setTheme(new DarculaTheme());
    LafManager.install();
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.TRACE);
    VirtualTcpServiceGUI virtualTcpServiceGUI = new VirtualTcpServiceGUI();
    VirtualTcpServiceConsole console = new VirtualTcpServiceConsole(
        virtualTcpServiceGUI.getConsole());
    console.setContext(root.getLoggerContext());
    console.start();
    root.addAppender(console);
    virtualTcpServiceGUI.setVisible(true);
  }
}
