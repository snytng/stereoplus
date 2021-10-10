package snytng.astah.plugin.stereoplus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import com.change_vision.jude.api.inf.view.IEntitySelectionEvent;
import com.change_vision.jude.api.inf.view.IEntitySelectionListener;
import com.change_vision.jude.api.inf.view.IViewManager;

public class View extends JPanel
implements IPluginExtraTabView, IEntitySelectionListener {
	/**
	 * logger
	 */
	static final Logger logger = Logger.getLogger(View.class.getName());
	static {
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.CONFIG);
		logger.addHandler(consoleHandler);
		logger.setUseParentHandlers(false);
	}

	/**
	 * プロパティファイルの配置場所
	 */
	private static final String VIEW_PROPERTIES = View.class.getPackage().getName() + ".view";

	/**
	 * リソースバンドル
	 */
	private static final ResourceBundle VIEW_BUNDLE = ResourceBundle.getBundle(VIEW_PROPERTIES, Locale.getDefault());

	private String title = "<Stereo+>";
	private String description = "<This plugin edits stereotypes quickly.>";

	private static final long serialVersionUID = 1L;
	private transient ProjectAccessor projectAccessor = null;
	private transient IDiagramViewManager diagramViewManager = null;

	public View() {
		try {
			projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
			diagramViewManager = projectAccessor.getViewManager().getDiagramViewManager();
		} catch (ClassNotFoundException | InvalidUsingException e){
			// no action
		}
		initComponents();
	}


	private void initComponents() {
		// レイアウトの設定
		setLayout(new BorderLayout());
		add(createEditRelationPane(), BorderLayout.CENTER);

		// リスナーへの登録
		addEntitySelectionListener();
	}

	private void setupStereotypeView(List<StereotypeView> svs){
		setupStereotypeButtons(svs);
		panelButtons.removeAll();
		for(StereotypeView sv : svs){
			panelButtons.add(sv.getBox());
		}
		panelButtons.revalidate();
	}

	private void defaultStereotypeView(){
		List<StereotypeView> svs = StereotypeViewFactory.getDefaultSteretypeViews();
		labelMessage.setText(VIEW_BUNDLE.getString("puglicExtraTabView.SetDefaultStereotypeDefinition"));
		setupStereotypeView(svs);
	}

	private void updateStereotypeView(){
		List<StereotypeView> svs = StereotypeViewFactory.getSteretypeViews();
		if(svs.isEmpty()){
			svs = StereotypeViewFactory.getDefaultSteretypeViews();
			labelMessage.setText(VIEW_BUNDLE.getString("puglicExtraTabView.NotStereotypeDefinition")
					+ " "
					+ VIEW_BUNDLE.getString("puglicExtraTabView.SetDefaultStereotypeDefinition"));
		} else {
			labelMessage.setText(VIEW_BUNDLE.getString("puglicExtraTabView.LoadNewStereotypeDefinition"));

		}
		setupStereotypeView(svs);
	}

	private void addEntitySelectionListener() {
		try {
			projectAccessor.getViewManager().getDiagramViewManager().addEntitySelectionListener(this);
		} catch (InvalidUsingException e) {
			// no action
		}
	}

	/*
	 * ステレオタイプ編集パネル作成
	 */
	private JLabel  labelMessage = null;
	private JPanel  panelButtons = null;
	private JRadioButton colorizeButton = null;

	private Container createEditRelationPane() {
		JPanel panelMain = new JPanel();
		panelMain.setLayout(new BorderLayout());

		JPanel panelLabel = new JPanel();
		panelLabel.setLayout(new BorderLayout());

		panelButtons = new JPanel();
		panelButtons.setLayout(new GridLayout(0,1));

		labelMessage = new JLabel(VIEW_BUNDLE.getString("puglicExtraTabView.pleaseSelectElements"));
		panelLabel.add(labelMessage, BorderLayout.WEST);

		JButton defaultButton  = new JButton("Default");
		defaultButton.addActionListener(e -> defaultStereotypeView());

		JButton updateButton  = new JButton("Update");
		updateButton.addActionListener(e -> updateStereotypeView());

		JPanel options = new JPanel();
		colorizeButton = new JRadioButton("Colorize");
		colorizeButton.setSelected(true);
		options.add(colorizeButton);

		JPanel buttons = new JPanel();
		buttons.add(options);
		buttons.add(defaultButton);
		buttons.add(updateButton);
		panelLabel.add(buttons, BorderLayout.EAST);


		defaultStereotypeView();

		panelMain.add(panelLabel, BorderLayout.NORTH);
		panelMain.add(panelButtons, BorderLayout.CENTER);

		return new JScrollPane(panelMain);
	}

	/**
	 * 要素の選択が変更されたら表示を更新する
	 */
	@Override
	public void entitySelectionChanged(IEntitySelectionEvent arg0) {
		try {
			// 今選択している図のタイプを取得する
			IViewManager vm = projectAccessor.getViewManager();
			IDiagramViewManager dvm = vm.getDiagramViewManager();
			IDiagram diagram = dvm.getCurrentDiagram();

			// 選択している図がクラス図ならば、ステレオタイプ編集パネルを更新する
			if(diagram instanceof IClassDiagram){
				updateClassDiagram(dvm, (IClassDiagram)diagram);
			}
		}catch(Exception ex){
			// no action
		}
	}

	private transient IClassDiagram classDiagram = null;
	private transient IPresentation[] selectedPresentations = null;
	private void updateClassDiagram(IDiagramViewManager dvm, IClassDiagram classDiagram) {
		labelMessage.setText(VIEW_BUNDLE.getString("puglicExtraTabView.pleaseSelectElements"));

		// クラス図を設定
		this.classDiagram = classDiagram;

		// 選択要素のリセット
		this.selectedPresentations = null;

		// ステレオタイプボタン色をリセット
		resetStereotypeButtons();

		// 選択要素の取得
		this.selectedPresentations = dvm.getSelectedPresentations();

		// 選択要素が複数の時、選択されたステレオタイプのボタン色を変更
		for(IPresentation p : selectedPresentations){
			if(p.getModel() instanceof IClass){
				indicateStereotypeButtons(p);
			}
		}

		// 選択要素が一つかつクラスの時、ラベルを表示
		if(selectedPresentations.length == 1){
			IPresentation p = selectedPresentations[0];
			if(p.getModel() instanceof IClass){
				IClass c = (IClass)p.getModel();
				labelMessage.setText(readStreotypeOfClass(c));
			}
		}
	}

	// ステレオタイプボタンの色をリセット
	private void resetStereotypeButtons() {
		JButton originalButton = new JButton();
		for (Map.Entry<String,JButton> entry : buttonMap.entrySet()) {
			JButton b = entry.getValue();
			b.setForeground(originalButton.getForeground());
			b.setBackground(originalButton.getBackground());
		}
	}


	// クラスのステレオタイプに対応するステレオタイプボタンの色を変更
	private void indicateStereotypeButtons(IPresentation p) {
		String[] ss = p.getModel().getStereotypes();
		for(String s : ss){
			if(buttonMap.containsKey(s)){
				JButton b = buttonMap.get(s);
				b.setForeground(Color.RED);
				b.setBackground(Color.RED);
			}
		}
	}

	// synchronized object
	private final transient Object[] syncEdit = new Object[0];

	// ActionListners
	private ActionListener getBActionListener(final String[] stereotypes, final int index){
		return ce -> {
			synchronized(syncEdit){

				for(IPresentation selectedPresentation : selectedPresentations){
					IElement e = selectedPresentation.getModel();

					// 選択されたクラスのステレオタイプを変更
					try {
						TransactionManager.beginTransaction();

						// 覚えていた色に設定し直す
						resetPresentationColor();

						// ステレオタイプを変更開始
						String[] allss = e.getStereotypes();
						String addstreotype = stereotypes[index];

						// 同じカテゴリのステレオタイプだけを削除
						for(String s : stereotypes){
							for(String ns : allss){
								if(s.equals(ns)){
									e.removeStereotype(s);
								}
							}
						}

						// 指定したステレオタイプを追加
						if(! addstreotype.isEmpty()){
							e.addStereotype(addstreotype);
						}

						TransactionManager.endTransaction();

					} catch (Exception ex) {
						ex.printStackTrace();
						TransactionManager.abortTransaction();
					}

					// クラスのステレオタイプの読み上げを更新
					if (e instanceof IClass){
						IClass c = (IClass)e;
						labelMessage.setText(readStreotypeOfClass(c));
					}
				}
			}
		};
	}

	private ChangeListener getHChangeListener(final String stereotypegroupname, final String[] stereotypes, final String[] fillColors){
		return ce -> {

			if(classDiagram == null){
				return;
			}

			if(! colorizeButton.isSelected()){
				return;
			}

			synchronized(syncEdit){

				JButton b = (JButton)ce.getSource();

				try {
					//TransactionManager.beginTransaction();

					if(b.getModel().isRollover()){
						StringBuilder hilight = new StringBuilder(stereotypegroupname);
						hilight.append(":");
						IPresentation[] ps = classDiagram.getPresentations();
						for(IPresentation p : ps){
							// ステレオタイプの色に変更する
							setPresentationColor(p, stereotypes, fillColors);
						}

						// ハイライトしたクラスを読み上げ
						labelMessage.setText(hilight.toString());

					} else {
						// 覚えていた色に設定し直す
						resetPresentationColor();
					}

					//TransactionManager.endTransaction();

				} catch (Exception ex) {
					ex.printStackTrace();
					//TransactionManager.abortTransaction();
				}
			}
		};
	}

	// 図要素の変更した要素を記憶
	transient List<IPresentation> hilightplist = new ArrayList<>();

	// 図要素に一時的な色をつける
	private void setPresentationColor(IPresentation p, String[] stereotypes, String[] fillColors) throws InvalidUsingException {
		IElement e = p.getModel();
		if(e == null){
			return;
		}
		String[] allss = e.getStereotypes();
		if(allss == null){
			return;
		}

		hilightplist.add(p);
		diagramViewManager.setViewProperty(
				p,
				IDiagramViewManager.BACKGROUND_COLOR,
				Color.WHITE);


		for(String ss: allss){
			for(int i = 0; i < stereotypes.length; i++){
				String stereotype = stereotypes[i];
				String fillColor  = fillColors[i];
				if(ss.equals(stereotype)){
					diagramViewManager.setViewProperty(
							p,
							IDiagramViewManager.BACKGROUND_COLOR,
							Color.decode(fillColor));
					break;
				}
			}
		}

	}

	// 図要素の一時的な色をもとに戻す
	private void resetPresentationColor() throws InvalidUsingException {
		while(! hilightplist.isEmpty()){
			int lastIndex = hilightplist.size() - 1;
			IPresentation p = hilightplist.get(lastIndex);
			diagramViewManager.clearAllViewProperties(p);
			hilightplist.remove(lastIndex);
		}
	}

	private transient Map<String, JButton> buttonMap = null;

	private void setupStereotypeButtons(List<StereotypeView> svs){
		buttonMap = new HashMap<>();
		for(StereotypeView sv : svs){

			sv.getButtonTop().addChangeListener(getHChangeListener(sv.getName(), sv.getNames(), sv.getColors()));
			sv.getButtonTop().setBackground(sv.getColor());
			sv.getButtons()[0].addActionListener(getBActionListener(sv.getNames(), 0));
			for(int i = 1; i < sv.getButtons().length; i++){
				sv.getButtons()[i].addActionListener(getBActionListener(sv.getNames(), i));
				sv.getButtons()[i].addChangeListener(getHChangeListener(sv.getName(), new String[]{sv.getNames()[i]}, new String[]{sv.getColors()[i]}));
			}

			for(JButton bs : sv.getButtons()){
				buttonMap.put(bs.getText(),bs);
			}
		}
	}


	private String readStreotypeOfClass(IClass c){
		String[] ss = c.getStereotypes();
		StringBuilder stereotype = new StringBuilder(c.getName());
		stereotype.append("のステレオタイプ：");
		for(String s : ss){
			stereotype.append(s);
			stereotype.append(" ");
		}
		return stereotype.toString();
	}

	@Override
	public void addSelectionListener(ISelectionListener listener) {
		// Pluginがタブを選択されても何もしない
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getDescription() {
		return VIEW_BUNDLE.getString("pluginExtraTabView.description");
	}
	@Override
	public String getTitle() {
		return VIEW_BUNDLE.getString("pluginExtraTabView.title");
	}

	@Override
	public void activated() {
		// Pluginが有効になっても何もしない
	}

	@Override
	public void deactivated() {
		// Pluginが無効になっても何もしない
	}
}
