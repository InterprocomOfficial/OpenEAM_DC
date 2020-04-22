package com.ipc.openeam.desktop.jfx.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ipc.openeam.desktop.App;
import com.ipc.openeam.desktop.bean.*;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.attribute.Attribute;
import com.ipc.openeam.desktop.bean.attribute.AttributeOnClassstructure;
import com.ipc.openeam.desktop.bean.attribute.AttributeValue;
import com.ipc.openeam.desktop.bean.classification.Classstructure;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.field.FieldValue;
import com.ipc.openeam.desktop.dao.AttributeValueDao;
import com.ipc.openeam.desktop.dao.FieldDao;
import com.ipc.openeam.desktop.dao.FieldValueDao;
import com.ipc.openeam.desktop.util.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class BasicController implements Initializable {
	@FXML protected Pane root;
	
	protected BasicController parentController;
	
	protected Logger log;
	protected EntityManager em;
	protected Injector injector;
	protected TableView<? extends SystemBean> tableView;
	private FieldValueDao fieldValueDao;
	private AttributeValueDao attrValueDao;


	public static final int attributeColumns = 2;
	
	public BasicController() {
		log = Logger.getLogger(getClass());
	}
	
	@Inject 
	public BasicController(EntityManager em, Injector injector) {
		this();
		this.em = em;
		this.injector = injector;
	}
	
	@Inject 
	public BasicController(EntityManager em, Injector injector, FieldValueDao fieldValueDao,
						   AttributeValueDao attrValueDao) {
		this(em, injector);
		this.fieldValueDao = fieldValueDao;
		this.attrValueDao = attrValueDao;
	}

	public Pane getRoot() {
		return root;
	}
	
	public Optional<? extends BasicController> getParentController() {
		return Optional.ofNullable(parentController);
	}

	public void setParentController(BasicController parentController) {
		this.parentController = parentController;
	}

	public TableView<? extends SystemBean> getTableView() {
		return tableView;
	}

	public void setTableView(TableView<? extends SystemBean> tableView) {
		this.tableView = tableView;
	}


	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
	}

	public void closeWindow() {
		Platform.runLater(() -> getStage().close());
	}

	protected void createBeanFields(BasicBean bean, FieldDao fieldDao) {
		new ArrayList<>(fieldDao.findByUseWith(bean.getClass())).stream().
		filter(field -> !bean.getFields().containsKey(field.getName())).
		forEach(field -> {
			FieldValue fieldValue = new FieldValue();
			fieldValue.setField(field);
			fieldValue.setBean(bean);
			bean.addField(fieldValue);
		});
	}

	protected Map<String, Node> fillUiFields(BasicBean bean, List<Node> elements, FieldDao fieldDao) {
		return fillUiFields(bean, elements, fieldDao, false);
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Node> fillUiFields(BasicBean bean, List<Node> elements, FieldDao fieldDao,
											 boolean originalNames) {
		Map<String, Node> elementsMapping = new HashMap<>();
		createBeanFields(bean, fieldDao);

		for (Node container : elements) {
			if (container.getId() != null && container instanceof Pane && !(container instanceof GridPane)) {
				Node element;
				Label title = null;
				
				if (((Pane) container).getChildren().size() > 1) {
					title = (Label) ((Pane) container).getChildren().get(0);
					element = ((Pane) container).getChildren().get(1);
				} else {
					element = ((Pane) container).getChildren().get(0);
				}

				elementsMapping.put(container.getId(), element);
				
				FieldValue fieldValue = bean.getField(container.getId());
				
				if (fieldValue != null) {
					if (title != null) {
						String fieldRequire = (String) container.getProperties().get("midRequire");
						String useFormName = (String) container.getProperties().get("midUseFormName");
						if (!originalNames && useFormName != null && useFormName.equals("true")) {
							originalNames = true;
						}

						title.setText(String.format("%s:", fieldValue.getField().getDescription()));

					}

						if (fieldValue.getValue(em) != null) {
							if (element instanceof CheckBox) {
								((CheckBox) element).setSelected((Boolean)fieldValue.getValue(em));
							} else if (element instanceof TextInputControl) {
								if (fieldValue.getField().getType().equals(BeanPropertyType.NUMERIC)) {
									((TextInputControl) element).setText(
											Utils.decimalFormat.format(fieldValue.getValue(em)));
								} else if (fieldValue.getField().getType().equals(BeanPropertyType.INTEGER)) {
									((TextInputControl) element).setText(
											Utils.integerFormat.format(fieldValue.getValue(em)));
								} else {
									((TextInputControl) element).setText(fieldValue.getValue(em).toString());
								}
							} else if (element instanceof HTMLEditor) {
								((HTMLEditor) element).setHtmlText((String) fieldValue.getValue(em));
							} else if (element instanceof DatePicker) {
								LocalDate dateValue = (LocalDate) fieldValue.getValue(em);
								((DatePicker) element).setConverter(Utils.getDatepickerConverter());
								if (dateValue != null) {
									((DatePicker) element).setValue(dateValue);
								}
							}
						}
				}
			}
		}
		return elementsMapping;
	}


	protected Map<String, VBox> fillUiAttributes(BasicBeanWithClass bean, TabPane tabPane, String defaultTabTitle,
												 String defaultPaneTitle, Map<String, ChangeListener<?>> changeListnes,
												 Map<String, Consumer<Pane>> inits) {
		Map<String, VBox> result = new HashMap<>();
		if (bean.getClassstructure() != null) {
			try {
				LinkedHashMap<String, LinkedHashMap<String, List<AttributeOnClassstructure>>> sections =
						buildAttributeSection(bean, null, defaultTabTitle, defaultPaneTitle);

				sections.forEach((sectionName, subSections) -> {
					GridPane section = createSectionTab(sectionName, sectionName, tabPane);
					AtomicInteger subSectionNum = new AtomicInteger(0);

					subSections.forEach((subSectionName, attrs) -> {
						int index = subSectionNum.getAndIncrement();
						TitledPane subSection = createSubSectionPane(subSectionName);
						section.add(subSection, index % attributeColumns,
								Math.floorDiv(index, attributeColumns));

						attrs.forEach(attr -> {
							try {
								VBox attibuteUI = getAttributeUI(attr, bean);

								if (inits != null && inits.containsKey(attr.getAttribute().getName())) {
									inits.get(attr.getAttribute().getName()).accept(attibuteUI);
								}

								if (changeListnes != null && changeListnes.containsKey(attr.getAttribute().getName())) {
									Control controlElement = (Control) attibuteUI.getChildren().get(1);
									if (controlElement instanceof TextField) {
										((TextField) controlElement).textProperty().addListener(
												(ChangeListener<? super String>) changeListnes.get(
														attr.getAttribute().getName()));
									} else if (controlElement instanceof DatePicker) {
										((DatePicker) controlElement).valueProperty().addListener(
												(ChangeListener<? super LocalDate>) changeListnes.get(
														attr.getAttribute().getName()));
									}
								}
								((VBox) subSection.getContent()).getChildren().add(attibuteUI);
								result.put(attr.getAttribute().getName(), attibuteUI);
							} catch (IllegalArgumentException e) {
								log.error("On build attribute UI component", e);
							}
						});
					});
				});
			} catch (IllegalArgumentException noOwnership) {
				return result;
			}
		}
		return result;
	}
	
	protected Map<String, VBox> fillUiAttributes(BasicBeanWithClass bean, TabPane tabPane, String defaultTabTitle,
												 String defaultPaneTitle) {
		return fillUiAttributes(bean, tabPane, defaultTabTitle, defaultPaneTitle, null, null);
	}

	protected LinkedHashMap<String, LinkedHashMap<String, List<AttributeOnClassstructure>>> buildAttributeSection(
			BasicBeanWithClass bean, Classstructure classstructure, String defaultSectionName,
			String defaultSubSectionName) {
		if (bean.getClassstructure().isPersisted() && classstructure == null) {
			classstructure = bean.getClassstructure();
		} else if (classstructure == null) {
			return new LinkedHashMap<>();
		}
		Stream<AttributeOnClassstructure> attributes;
		attributes = classstructure.getAttributesOnClass().stream();

		return attributes.map(attrOnClass -> {
			if (attrOnClass.getSection() == null || attrOnClass.getSection().isEmpty()) {
				attrOnClass.setSection(defaultSectionName);
			}
			if (attrOnClass.getSubSection() == null || attrOnClass.getSubSection().isEmpty()) {
				attrOnClass.setSubSection(defaultSubSectionName);
			}
			return attrOnClass;
		}).sorted(Comparator.comparing(AttributeOnClassstructure::getSectionPosition)
				.thenComparing(AttributeOnClassstructure::getSubSectionPosition)
				.thenComparing(AttributeOnClassstructure::getAttributePosition))
			.collect(Collectors.groupingBy(AttributeOnClassstructure::getSection, LinkedHashMap::new,
					Collectors.groupingBy(AttributeOnClassstructure::getSubSection,
							LinkedHashMap::new, Collectors.toList())));
	}
	
	@SuppressWarnings("unchecked")
	protected boolean fillBeanFields(BasicBean bean, List<Node> elements) {
		boolean objectChanged = false;
		for (Node container : elements) {
			if (container.getId() != null && container instanceof Pane) {
				Node element;
				if (((Pane) container).getChildren().size() > 1) {
					element = ((Pane) container).getChildren().get(1);
				} else {
					element = ((Pane) container).getChildren().get(0);
				}
				FieldValue fieldValue = bean.getField(container.getId());
				if (fieldValue != null) {
					String previousValueAsString = fieldValue.getValueAsString(em);
					boolean changed = false;

						if (element instanceof CheckBox) {
							changed = fieldValue.setValue(((CheckBox) element).isSelected());
						} else if (element instanceof TextInputControl) {
							changed = fieldValue.setValue(((TextInputControl) element).getText());
						} else if (element instanceof HTMLEditor) {
							changed = fieldValue.setValue(((HTMLEditor) element).getHtmlText());
						} else if (element instanceof DatePicker) {
							changed = fieldValue.setValue(((DatePicker) element).getValue());
						}
				}
			}
		}
		return objectChanged;
	}

	protected boolean fillBeanAttributes(BasicBeanWithClass bean, TabPane tabPane) {
		return getUIAttributes(tabPane).map(attr -> changeAttribute(attr, bean))
				.filter(r -> r.equals(true)).count() > 0;
	}
	
	protected Stage getStage() {
		return (Stage) getRoot().getScene().getWindow();
	}

	protected TitledPane createSubSectionPane(String title) {
		TitledPane titledPane = new TitledPane(title, new VBox());
		titledPane.getStyleClass().add("attributePane");

		GridPane.setValignment(titledPane, VPos.TOP);
		titledPane.setPrefWidth(Float.POSITIVE_INFINITY);
		return titledPane;
	}

	@SuppressWarnings("unchecked")
	private boolean changeAttribute(VBox formFieldNode, BasicBeanWithClass bean) {
		String attrId = formFieldNode.getId();
		AttributeOnClassstructure attrOnClass = bean.getClassstructure().findAttrOnClass(attrId);
		if (attrOnClass == null) {
			return false;
		}
		
		Node formFieldValue = formFieldNode.getChildren().get(1);
		AttributeValue attrValue = bean.findAttributeByName(formFieldNode.getId());
		
		if (attrValue == null) {
			attrValue = injector.getInstance(AttributeValue.class);
			attrValue.setAttributeOnClass(attrOnClass);
			attrValue.setBean(bean);
			bean.addAttribute(attrValue);
		}

		String previousValueAsString = attrValue.getValueAsString(em);
		boolean changed = false;

			if (attrOnClass.getAttribute().getType().equals(BeanPropertyType.YORN)) {
				changed = attrValue.setValue(((CheckBox)formFieldValue).isSelected());
			} else {
				changed = attrValue.setValue(((TextField)formFieldValue).getText());
			}
		return changed;
	}

	private Stream<VBox> getUIAttributes(TabPane tabPane) {
		return tabPane.getTabs().stream().filter(
				tab -> tab.getStyleClass().contains("attributeTab")).flatMap(
				tab -> ((GridPane)((ScrollPane)
						tab.getContent()).getContent()).getChildren().stream()).flatMap(
				subSection -> ((VBox)(((TitledPane) subSection).getContent())).getChildren()
						.stream())
				.filter(VBox.class::isInstance).map(VBox.class::cast);
	}

	private GridPane createSectionTab(String title, String id, TabPane tabPane) {
		Tab tab = new Tab(title);
		tab.setId(id);
		tab.getStyleClass().add("attributeTab");

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(25, 25, 10, 25));

		scrollPane.setContent(gridPane);
		tab.setContent(scrollPane);

		tabPane.getTabs().add(getLastAttributeTabIndex(tabPane), tab);
		return gridPane;
	}
	
	private int getLastAttributeTabIndex(TabPane tabPane) {
		return tabPane.getTabs().stream().mapToInt(tab ->
				tab.getStyleClass().contains("attributeTab") ? 1 : 0).sum() + 1;
	}

	private VBox getAttributeUI(AttributeOnClassstructure attrOnClass, BasicBeanWithClass bean) {
		VBox attributeUI = new VBox();
		
		Attribute attribute = attrOnClass.getAttribute();
		
		attributeUI.getStyleClass().add("beanAttribute");
		attributeUI.setId(attribute.getName());
		
		// Установка названия поля атрибута
		Label attrDescription = new Label();
		attrDescription.setText(attrOnClass.getAttribute().getDescription());
		attributeUI.getChildren().add(attrDescription);
		
		AttributeValue attrValue = bean.findAttributeByName(attribute.getName());
		Object attrValueObject = null;
		if (attrValue != null) {
			attrValueObject = attrValue.getValue(em);
		} else {
			attrValue = new AttributeValue();
			attrValue.setAttributeOnClass(attrOnClass);
			attrValue.setBean(bean);
		}

		Node attributeField;
			if (attribute.getType().equals(BeanPropertyType.YORN)) {
				attributeField = new CheckBox();
				attributeField.setId(attribute.getName());
				if (attrValueObject != null) {
					((CheckBox) attributeField).setSelected((Boolean) attrValueObject);
				}
			} else if (attribute.getType().equals(BeanPropertyType.ALN)) {
				attributeField = new TextField();
				attributeField.setId(attribute.getName());
				if (attrValueObject != null) {
					((TextField) attributeField).setText(attrValueObject.toString());
				}
			} else if (attribute.getType().equals(BeanPropertyType.NUMERIC)) {
				attributeField = new TextField();
				attributeField.setId(attribute.getName());
				if (attrValueObject != null) {
					((TextField) attributeField).setText(Utils.decimalFormat.format(attrValueObject));
				}
			} else {
				throw new IllegalArgumentException(String.format("Attribute %s can not be displayed", attribute));
			}

		attributeUI.getChildren().add(attributeField);

		
		return attributeUI;
	}

}
