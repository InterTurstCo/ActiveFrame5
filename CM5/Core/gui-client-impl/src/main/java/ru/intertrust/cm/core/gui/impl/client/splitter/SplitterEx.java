package ru.intertrust.cm.core.gui.impl.client.splitter;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionPluginResizeBySplitterEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;


public class SplitterEx extends DockLayoutPanel {
    private static final int CENTRAL_PANEL_TOP_MARGIN = 70;
    private static final int TOOLBAR_HEIGHT = 40;
    protected boolean splitType;
    protected int sizeFromInsert;

    class HSplitter extends Splitter {
        public HSplitter(Widget target, boolean reverse, int size) {
            super(target, reverse);

            getElement().getStyle().setPropertyPx("width", splitterSize);
            leftArrow.setStyleName(GlobalThemesManager.getSplitterStyles().splitterArrow() + " " + GlobalThemesManager.getSplitterStyles().leftArrowVert());
            rightArrow.setStyleName(GlobalThemesManager.getSplitterStyles().splitterArrow() + " " + GlobalThemesManager.getSplitterStyles().rightArrowVert());
            centralDummy.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelVertDots());
            changeModeButton.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeVertButton());
            splitType = true;
            if (size == 0) {
                changeFullLeftPosition();
                rightArrowVisible(false);
            } else if (size == target.getParent().getParent().getOffsetWidth() - splitterSize) {

                changeFullRightPosition();
                leftArrowVisible(false);
            } else {
                changeCentralPanel();

            }


        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteLeft();
        }

        @Override
        protected double getCenterSize() {
            return getCenterWidth();
        }

        @Override
        protected int getEventPosition(Event event) {
            return event.getClientX();
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteLeft();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetWidth();
        }

        @Override
        protected void changeCentralPanel() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelVert());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeVert());
            setStyleName(GlobalThemesManager.getSplitterStyles().horizontalBar());
        }

        @Override
        protected void changeFullLeftPosition() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelVertLeft());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeVertLeft());
            setStyleName(GlobalThemesManager.getSplitterStyles().horizontalBarLeft());
        }

        @Override
        protected void changeFullRightPosition() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelVertRight());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeVertRight());
            setStyleName(GlobalThemesManager.getSplitterStyles().horizontalBarRight());
        }

    }

    abstract class Splitter extends FlowPanel {

        protected final FlowPanel leftArrow = new FlowPanel();
        protected final FlowPanel rightArrow = new FlowPanel();

        protected final FlowPanel centralPanel = new FlowPanel();
        protected final FlowPanel changeModePanel = new FlowPanel();
        protected final FlowPanel changeModeButton = new FlowPanel();

        protected final FlowPanel centralDummy = new FlowPanel();
        private FlowPanel dummyLeftPanel = new FlowPanel();
        private FlowPanel dummySplitter = new FlowPanel();
        private FlowPanel dummyLeftButton = new FlowPanel();
        private FlowPanel dummyRightButton = new FlowPanel();

        protected final Widget target;

        private int offset;
        private boolean mouseDown;
        private Scheduler.ScheduledCommand layoutCommand;

        private final boolean reverse;
        private int minSize;
        private int snapClosedSize = -1;
        private double centerSize, syncedCenterSize;

        private boolean toggleDisplayAllowed = false;
        private double lastClick = 0;

        public Splitter(Widget target, boolean reverse) {
            GlobalThemesManager.getSplitterStyles().ensureInjected();
            this.target = target;
            target.getElement().addClassName("target-target");
            target.getElement().getParentElement().addClassName("before-target-target");
            this.reverse = reverse;
            target.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

            dummyLeftPanel.setStyleName(GlobalThemesManager.getSplitterStyles().touchDummyButton());
            dummyLeftButton.setStyleName(GlobalThemesManager.getSplitterStyles().touchDummyButton());
            dummyRightButton.setStyleName(GlobalThemesManager.getSplitterStyles().touchDummyButton());
            dummySplitter.setStyleName(GlobalThemesManager.getSplitterStyles().touchDummySplitter());

            leftArrow.add(dummyLeftButton);
            rightArrow.add(dummyRightButton);
            changeModeButton.add(dummyLeftPanel);
            add(dummySplitter);

            changeModePanel.add(changeModeButton);

            centralPanel.add(rightArrow);
            centralPanel.add(leftArrow);

            panelOnMouseEvent(false);

            add(centralDummy);
            add(centralPanel);
            add(changeModePanel);

            leftArrow.setVisible(true);
            rightArrow.setVisible(true);


            getElement().setId("gwt-splitter");
            sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE
                    | Event.ONDBLCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);

            buttonEvent();

        }

        private void invertFullSize() {
            if (fullSize)
                fullSize = false;
            else
                fullSize = true;
        }

        protected void buttonEvent() {
            //правая кнопка (вниз)
            leftArrow.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    eventBus.fireEvent(new CollectionPluginResizeBySplitterEvent());
                    //TODO DELETE FOREIGN CODE

                    eventBus.fireEvent(new SplitterWidgetResizerEvent(target.getParent().getParent().getOffsetWidth() - splitterSize,
                            0,
                            (fullSize) ? customSize : target.getParent().getOffsetHeight(),
                            0, splitType, true));
                    invertFullSize();

                }
            }, ClickEvent.getType());

            //на самом деле это левая кнопка (Тим) (вверх)
            rightArrow.addDomHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    eventBus.fireEvent(new CollectionPluginResizeBySplitterEvent());
                    eventBus.fireEvent(new SplitterWidgetResizerEvent(0, 0,
                            (fullSize)?customSize:splitterSize,
                            target.getParent().getOffsetHeight(),
                            splitType, true));

                    invertFullSize();
                }
            }, ClickEvent.getType());

            changeModeButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    eventBus.fireEvent(new CollectionPluginResizeBySplitterEvent());
                    int firstWidgetWidth = target.getParent().getParent().getOffsetWidth() / 2 - DEFAULT_SPLITTER_SIZE;
                    if (splitType) {
                        splitType = false;

                    } else {
                        splitType = true;
                        if (sizeFromInsert != target.getParent().getParent().getOffsetWidth() / 2 && sizeFromInsert > 0) {
                            firstWidgetWidth = sizeFromInsert - DEFAULT_SPLITTER_SIZE;
                        }
                    }


                    eventBus.fireEvent(new SplitterWidgetResizerEvent(firstWidgetWidth, 0,
                            target.getParent().getParent().getOffsetHeight() / 2, target.getParent().getParent().getOffsetHeight(), splitType, false));

                }
            }, ClickEvent.getType());

        }

        protected void rightArrowVisible(boolean b) {
            rightArrow.setVisible(b);

        }

        protected void leftArrowVisible(boolean b) {
            leftArrow.setVisible(b);
        }


        @Override
        public void onBrowserEvent(Event event) {
            switch (event.getTypeInt()) {
                case Event.ONMOUSEDOWN:
                    mouseDown = true;

                    int width = Math.max(Window.getClientWidth(),
                            Document.get().getScrollWidth());
                    int height = Math.max(Window.getClientHeight(),
                            Document.get().getScrollHeight());
                    glassElem.getStyle().setHeight(height, Style.Unit.PX);
                    glassElem.getStyle().setWidth(width, Style.Unit.PX);
                    Document.get().getBody().appendChild(glassElem);

                    offset = getEventPosition(event) - getAbsolutePosition();
                    Event.setCapture(getElement());
                    event.preventDefault();
                    break;

                case Event.ONMOUSEUP:
                    mouseDown = false;

                    glassElem.removeFromParent();
                    eventBus.fireEvent(new CollectionPluginResizeBySplitterEvent());

                    if (this.toggleDisplayAllowed) {
                        double now = Duration.currentTimeMillis();
                        if (now - this.lastClick < DOUBLE_CLICK_TIMEOUT) {
                            now = 0;
                            LayoutData layout = (LayoutData) target.getLayoutData();
                            if (layout.size == 0) {
                                // Restore the old size.
                                setAssociatedWidgetSize(layout.oldSize);

                                layout.oldSize = layout.size;
                                setAssociatedWidgetSize(0);
                            }
                        }
                        this.lastClick = now;
                    }

                    Event.releaseCapture(getElement());
                    event.preventDefault();
                    break;

                case Event.ONMOUSEMOVE:
                    if (mouseDown) {


                        if (splitType == true) {
                            if (target.getOffsetWidth() == 0) {
                                this.changeFullLeftPosition();
                                rightArrowVisible(false);

                            } else if (target.getOffsetWidth() <
                                    (getElement().getParentElement().getParentElement().getOffsetWidth() - this.getOffsetWidth())) {
                                this.changeCentralPanel();
                                leftArrowVisible(true);
                                rightArrowVisible(true);
                            } else {
                                this.changeFullRightPosition();
                                leftArrowVisible(false);
                            }
                        } else {
                            if (target.getOffsetHeight() == 0) {
                                this.changeFullLeftPosition();
                                rightArrowVisible(false);
                            } else if (target.getOffsetHeight() <
                                    (getElement().getParentElement().getParentElement().getOffsetHeight() - this.getOffsetHeight())) {
                                this.changeCentralPanel();
                                leftArrowVisible(true);
                                rightArrowVisible(true);
                            } else {
                                this.changeFullRightPosition();
                                leftArrowVisible(false);
                            }

                        }

                        int size;
                        if (reverse) {
                            size = getTargetPosition() + getTargetSize()
                                    - getEventPosition(event) - offset;
                        } else {
                            size = getEventPosition(event) - getTargetPosition() - offset;
                        }
                        ((LayoutData) target.getLayoutData()).hidden = false;
                        setAssociatedWidgetSize(size);
                        event.preventDefault();
                    }
                    break;
                case Event.ONMOUSEOVER:
                    panelOnMouseEvent(true);
                    event.preventDefault();


                    break;

                case Event.ONMOUSEOUT:
                    panelOnMouseEvent(false);
                    event.preventDefault();
                    break;
            }


        }

        public void panelOnMouseEvent(Boolean b) {
            if (b == true) {
                centralPanel.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                changeModePanel.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                //
            } else {
                centralPanel.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                changeModePanel.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
            }

        }


        public void setMinSize(int minSize) {
            this.minSize = minSize;
            LayoutData layout = (LayoutData) target.getLayoutData();

            // Try resetting the associated widget's size, which will enforce the new
            // minSize value.
            setAssociatedWidgetSize((int) layout.size);
        }

        public void setSnapClosedSize(int snapClosedSize) {
            this.snapClosedSize = snapClosedSize;
        }

        public void setToggleDisplayAllowed(boolean allowed) {
            this.toggleDisplayAllowed = allowed;
        }

        protected abstract int getAbsolutePosition();

        protected abstract double getCenterSize();

        protected abstract int getEventPosition(Event event);

        protected abstract int getTargetPosition();

        protected abstract int getTargetSize();

        protected abstract void changeCentralPanel();

        protected abstract void changeFullLeftPosition();

        protected abstract void changeFullRightPosition();

        private double getMaxSize() {

            double newCenterSize = getCenterSize();
            if (syncedCenterSize != newCenterSize) {
                syncedCenterSize = newCenterSize;
                centerSize = newCenterSize;
            }

            return Math.max(((LayoutData) target.getLayoutData()).size + centerSize,
                    0);
        }

        private void setAssociatedWidgetSize(double size) {
            double maxSize = getMaxSize();
            if (size > maxSize) {
                size = maxSize;
            }

            if (snapClosedSize > 0 && size < snapClosedSize) {
                size = 0;
            } else if (size < minSize) {
                size = minSize;
            }

            LayoutData layout = (LayoutData) target.getLayoutData();
            if (size == layout.size) {
                return;
            }

            // Adjust our view until the deferred layout gets scheduled.
            centerSize += layout.size - size;
            layout.size = size;

            // Defer actually updating the layout, so that if we receive many
            // mouse events before layout/paint occurs, we'll only update once.
            if (layoutCommand == null) {
                layoutCommand = new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        layoutCommand = null;
                        forceLayout();
                    }
                };
                Scheduler.get().scheduleDeferred(layoutCommand);
            }
        }


    }

    class VSplitter extends Splitter {
        public VSplitter(Widget target, boolean reverse, int size) {
            super(target, reverse);
            getElement().getStyle().setPropertyPx("height", splitterSize);
            leftArrow.setStyleName(GlobalThemesManager.getSplitterStyles().splitterArrow() + " " + GlobalThemesManager.getSplitterStyles().leftArrowHoriz());
            rightArrow.setStyleName(GlobalThemesManager.getSplitterStyles().splitterArrow() + " " + GlobalThemesManager.getSplitterStyles().rightArrowHoriz());
            centralDummy.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelHorizDots());
            changeModeButton.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeHorizButton());
            splitType = false;
            if (size == (0/*-splitterSize*/)) {
                changeFullLeftPosition();
                rightArrowVisible(false);
            } else if (size == target.getParent().getOffsetHeight() - splitterSize
                    || Window.getClientHeight() == size + CENTRAL_PANEL_TOP_MARGIN + TOOLBAR_HEIGHT + splitterSize) {
                changeFullRightPosition();
                leftArrowVisible(false);
            } else {
                changeCentralPanel();

            }

        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteTop();
        }

        @Override
        protected double getCenterSize() {
            return getCenterHeight();
        }

        @Override
        protected int getEventPosition(Event event) {
            return event.getClientY();
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteTop();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetHeight();
        }

        @Override
        protected void changeCentralPanel() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelHoriz());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeHoriz());
            getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
            setStyleName(GlobalThemesManager.getSplitterStyles().verticalBar());

        }

        @Override
        protected void changeFullLeftPosition() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelHorizTop());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeHorizTop());
            setStyleName(GlobalThemesManager.getSplitterStyles().verticalBarTop());
        }

        @Override
        protected void changeFullRightPosition() {
            centralPanel.setStyleName(GlobalThemesManager.getSplitterStyles().centralPanelHorizBottom());
            changeModePanel.setStyleName(GlobalThemesManager.getSplitterStyles().changeModeHorizBottom());
            setStyleName(GlobalThemesManager.getSplitterStyles().verticalBarBottom());
        }

    }

    private static final int DEFAULT_SPLITTER_SIZE = 8;
    private static final int DOUBLE_CLICK_TIMEOUT = 500;
    EventBus eventBus;

    /**
     * The element that masks the screen so we can catch mouse events over
     * iframes.
     */
    private static Element glassElem = null;

    private final int splitterSize;
    private int customSize;
    private boolean fullSize = false;

    public SplitterEx(int splitterSize, EventBus eventBus) {
        super(Style.Unit.PX);
        this.splitterSize = splitterSize;
        this.eventBus = eventBus;
        if (glassElem == null) {
            glassElem = Document.get().createDivElement();
            glassElem.getStyle().setPosition(Style.Position.ABSOLUTE);
            glassElem.getStyle().setTop(0, Style.Unit.PX);
            glassElem.getStyle().setLeft(0, Style.Unit.PX);
            glassElem.getStyle().setMargin(0, Style.Unit.PX);
            glassElem.getStyle().setPadding(0, Style.Unit.PX);
            glassElem.getStyle().setBorderWidth(0, Style.Unit.PX);

            glassElem.getStyle().setOpacity(0.0);
        }
    }

    /**
     * Return the size of the splitter in pixels.
     *
     * @return the splitter size
     */
    public int getSplitterSize() {
        return splitterSize;
    }

    public int getCustomSize() {
        return customSize;
    }

    public void setCustomSize(int customSize) {
        this.customSize = customSize;
    }

    @Override
    public void insert(Widget child, Direction direction, double size, Widget before) {
        super.insert(child, direction, size, before);
        if (direction != Direction.CENTER) {
            insertSplitter(child, before, (int) size);
        }

    }

    @Override
    public boolean remove(Widget child) {
        assert !(child instanceof Splitter) : "Splitters may not be directly removed";

        int idx = getWidgetIndex(child);
        if (super.remove(child)) {
            // Remove the associated splitter, if any.
            // Now that the widget is removed, idx is the index of the splitter.
            if (idx < getWidgetCount()) {
                // Call super.remove(), or we'll end up recursing.
                super.remove(getWidget(idx));
            }
            return true;
        }
        return false;
    }

    @Override
    public void setWidgetHidden(Widget widget, boolean hidden) {
        super.setWidgetHidden(widget, hidden);
        Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            // The splitter is null for the center element.
            super.setWidgetHidden(splitter, hidden);
        }
    }

    /**
     * Sets the minimum allowable size for the given widget.
     * <p/>
     * <p>
     * Its associated splitter cannot be dragged to a position that would make it
     * smaller than this size. This method has no effect for the
     * {@link com.google.gwt.user.client.ui.DockLayoutPanel.Direction#CENTER} widget.
     * </p>
     *
     * @param child   the child whose minimum size will be set
     * @param minSize the minimum size for this widget
     */
    public void setWidgetMinSize(Widget child, int minSize) {
        // assertIsChild(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setMinSize(minSize);
        }
    }

    /**
     * Sets a size below which the slider will close completely. This can be used
     * in conjunction with {@link #setWidgetMinSize} to provide a speed-bump
     * effect where the slider will stick to a preferred minimum size before
     * closing completely.
     * <p/>
     * <p>
     * This method has no effect for the {@link com.google.gwt.user.client.ui.DockLayoutPanel.Direction#CENTER}
     * widget.
     * </p>
     *
     * @param child          the child whose slider should snap closed
     * @param snapClosedSize the width below which the widget will close or
     *                       -1 to disable.
     */
    public void setWidgetSnapClosedSize(Widget child, int snapClosedSize) {
        //  assertIsChild(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setSnapClosedSize(snapClosedSize);
        }
    }

    /**
     * Sets whether or not double-clicking on the splitter should toggle the
     * display of the widget.
     *
     * @param child   the child whose display toggling will be allowed or not.
     * @param allowed whether or not display toggling is allowed for this widget
     */
    public void setWidgetToggleDisplayAllowed(Widget child, boolean allowed) {
        //  assertIsChild(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setToggleDisplayAllowed(allowed);
        }
    }

    private Splitter getAssociatedSplitter(Widget child) {
        // If a widget has a next sibling, it must be a splitter, because the only
        // widget that *isn't* followed by a splitter must be the CENTER, which has
        // no associated splitter.
        int idx = getWidgetIndex(child);
        if (idx > -1 && idx < getWidgetCount() - 1) {
            Widget splitter = getWidget(idx + 1);
            assert splitter instanceof Splitter : "Expected child widget to be splitter";
            return (Splitter) splitter;
        }
        return null;
    }

    private void insertSplitter(Widget widget, Widget before, int size) {
        assert getChildren().size() > 0 : "Can't add a splitter before any children";


        LayoutData layout = (LayoutData) widget.getLayoutData();

        Splitter splitter = null;
        switch (getResolvedDirection(layout.direction)) {
            case WEST:
                splitter = new HSplitter(widget, false, size);
                break;
            case EAST:
                splitter = new HSplitter(widget, true, size);
                break;
            case NORTH:
                splitter = new VSplitter(widget, false, size);
                break;
            case SOUTH:
                splitter = new VSplitter(widget, true, size);
                break;
            default:
                assert false : "Unexpected direction";
        }

        super.insert(splitter, layout.direction, splitterSize, before);

        //8px
        super.getWidgetContainerElement(splitter).getStyle().clearOverflow();
        this.setStyleName("tested");
        this.getElement().getStyle().clearPosition();

    }

    public boolean isSplitType() {
        return splitType;
    }

    public void setSizeFromInsert(int sizeFromInsert) {
        this.sizeFromInsert = sizeFromInsert;
    }
}
