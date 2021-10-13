package snytng.astah.plugin.stereoplus;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class StereotypeViewFactory {

	private StereotypeViewFactory(){}

	static ProjectAccessor projectAccessor = null;
	static IDiagramViewManager diagramViewManager = null;

	static {
		try {
			projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
			diagramViewManager = projectAccessor.getViewManager().getDiagramViewManager();
		} catch (ClassNotFoundException | InvalidUsingException e){
			// no action
		}
	}

	/*
	 * ステレオタイプ一覧
	 */
	static StereotypeView svClass = new StereotypeView(
			"クラス",
			"#FFFF99",
			new String[]{
					"",
					"abstract",
					"interface",
					"realization",
					"utility"
			},
			new String[]{
					"#FFFFFF",	// 255 255 255
					"#FFFF99",	// 255 255 153
					"#CCFFCC",  // 204 255 204
					"#CCFFFF",  // 204 255 255
					"#9999FF"   // 153 153 255
			});

	static StereotypeView svUsecase = new StereotypeView(
			"ユースケース",
			"#FF3366",
			new String[]{
					"",
					"actor"
			},
			new String[]{
					"#FFFFFF",	// 255 255 255
					"#FF3366"	// 255 51 102
			});

	static StereotypeView svRobustness = new StereotypeView(
			"ロバストネス",
			"#99CCFF",
			new String[]{
					"",
					"boundary",
					"control",
					"entity"
			},
			new String[]{
					"#FFFFFF",	// 255 255 255
					"#99CCFF",	// 153 204 255
					"#FFCCCC",  // 255 204 204
					"#FFCC33"   // 255 204 51
			});

	// デフォルトのステレオタイプを生成する
	public static List<StereotypeView> getDefaultSteretypeViews(){
		List<StereotypeView> svs = new ArrayList<>();
		svs.add(svClass);
		svs.add(svUsecase);
		svs.add(svRobustness);
		return svs;
	}

	// updateテスト用
	private static boolean updateFlag = true;
	public static List<StereotypeView> getSteretypeViewsTest(){
		List<StereotypeView> svs = new ArrayList<>();
		if(updateFlag){
			updateFlag = false;
			svs.add(svClass);
			return svs;
		} else {
			updateFlag = true;
			return getDefaultSteretypeViews();
		}
	}

	// ステレオタイプ定義図からステレオタイプを生成する
	// _stereotype\definition クラス図から情報を収集する
	public static List<StereotypeView> getSteretypeViews(){
		List<StereotypeView> svs = new ArrayList<>();

		try {
			IModel root = projectAccessor.getProject();

			for(INamedElement elem : root.getOwnedElements()){
				if(! (elem instanceof IPackage)) continue;
				if(! elem.getName().equals("_stereotypes")) continue;

				for(IDiagram d : ((IPackage) elem).getDiagrams()){
					if(! (d instanceof IClassDiagram)) continue;
					if(! d.getName().equals("definition")) continue;

					// _stereotype/definitionクラス図
					IClassDiagram cd = (IClassDiagram)d;
					IPresentation[] ps;

					ps = cd.getPresentations();
					Map<INodePresentation, List<INodePresentation>> cs = new LinkedHashMap<>();
					List<INodePresentation> nps = new ArrayList<>();

					// package
					for(IPresentation p : ps){
						if(p.getModel() instanceof IPackage){
							INodePresentation np = (INodePresentation)p;
							nps.add(np);
						}
					}
					Collections.sort(nps, new PackageCompartor());

					for(INodePresentation np : nps){
						cs.put(np, new ArrayList<INodePresentation>());
					}

					// class
					for(IPresentation p : ps){
						if(p.getModel() instanceof IClass){
							INodePresentation np = (INodePresentation)p;
							Point2D point = np.getLocation();

							for(INodePresentation pkg : nps){
								Point2D pkgpoint = pkg.getLocation();
								double h = pkg.getHeight();
								if(pkgpoint.getY() <=  point.getY() && pkgpoint.getY() + h >= point.getY()){
									List<INodePresentation> classes = cs.get(pkg);
									classes.add(np);
								}
							}

						}
					}

					// create SteretypeView list
					for(INodePresentation pkg : nps){
						Collections.sort(cs.get(pkg), new ClassCompartor());

						String name = ((IPackage)pkg.getModel()).getName();
						String color = pkg.getProperty("fill.color");

						String[] names = new String[cs.get(pkg).size()+1];
						String[] colors = new String[cs.get(pkg).size()+1];
						names[0] = "";
						colors[0] = "#FFFFFF";
						int index = 1;
						for(INodePresentation cls : cs.get(pkg)){
							String name1 = ((IClass)cls.getModel()).getName();
							String color1 = cls.getProperty("fill.color");
							names[index] = name1;
							colors[index] = color1;
							index++;
						}
						svs.add(new StereotypeView(name, color, names, colors));
					}


				}
			}

		} catch (InvalidUsingException e) {
			e.printStackTrace();

		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
		}

		return svs;
	}

	static class PackageCompartor implements Comparator<INodePresentation>{
		@Override
		public int compare(INodePresentation o1, INodePresentation o2) {
			Point2D p1 = o1.getLocation();
			Point2D p2 = o2.getLocation();
			if(p1.getY() > p2.getY()){
				return 1;
			} else if(p1.getY() < p2.getY()){
				return -1;
			} else {
				return 0;
			}
		}
	}

	static class ClassCompartor implements Comparator<INodePresentation>{
		@Override
		public int compare(INodePresentation o1, INodePresentation o2) {
			Point2D p1 = o1.getLocation();
			Point2D p2 = o2.getLocation();
			if(p1.getX() > p2.getX()){
				return 1;
			} else if(p1.getX() < p2.getX()){
				return -1;
			} else {
				return 0;
			}
		}
	}

}
