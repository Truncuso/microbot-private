package net.runelite.client.plugins.microbot.util.widget;

import net.runelite.api.MenuAction;
<<<<<<< HEAD
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class Rs2Widget {

    public static boolean clickWidget(String text) {
        Widget widget = findWidget(text, null);
=======
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

public class Rs2Widget {

    public static boolean sleepUntilHasWidgetText(String text, int widgetId, int childId, boolean exact, int sleep) {
        return sleepUntilTrue(() -> hasWidgetText(text, widgetId, childId, exact), 300, sleep);
    }

    public static boolean sleepUntilHasNotWidgetText(String text, int widgetId, int childId, boolean exact, int sleep) {
        return sleepUntilTrue(() -> !hasWidgetText(text, widgetId, childId, exact), 300, sleep);
    }


    public static boolean hasWidgetText(String text, int widgetId, int childId, boolean exact) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget rootWidget = getWidget(widgetId, childId);
            Widget widget = null;
            if (rootWidget.getChildren() != null)
                widget = findWidget(text, Arrays.stream(rootWidget.getChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
            if (rootWidget.getNestedChildren().length > 0)
                widget =  findWidget(text, Arrays.stream(rootWidget.getNestedChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
            if (rootWidget.getDynamicChildren().length > 0)
                widget = findWidget(text, Arrays.stream(rootWidget.getDynamicChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
            if (rootWidget.getStaticChildren().length > 0)
                widget = findWidget(text, Arrays.stream(rootWidget.getStaticChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);

            return widget != null;
        });
    }


    public static boolean clickWidget(String text, Optional<Integer> widgetId, int childId, boolean exact) {
        return Microbot.getClientThread().runOnClientThread(() -> {

            Widget widget;
            if (!widgetId.isPresent()) {
                widget = findWidget(text, null, exact);
            } else {
                Widget rootWidget = getWidget(widgetId.get(), childId);
                List<Widget> rootWidgets = new ArrayList<>();
                rootWidgets.add(rootWidget);
                widget  = findWidget(text, rootWidgets, true);
            }

            if (widget != null) {
                clickWidget(widget);
            }

            return widget != null;

        });
    }

    public static boolean clickWidget(Widget widget) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
        if (widget != null) {
            Microbot.getMouse().click(widget.getBounds());
            return true;
        }
        return false;
    }

<<<<<<< HEAD
    public static boolean clickWidget(String text, boolean exact) {
        Widget widget = findWidget(text, null, exact);
        if (widget != null) {
            Microbot.getMouse().click(widget.getBounds());
            return true;
        }
        return false;
=======
    public static boolean clickWidget(String text) {
        return clickWidget(text, Optional.empty(), 0, false);
    }

    public static boolean clickWidget(String text, boolean exact) {
        return clickWidget(text, Optional.empty(), 0, exact);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    public static boolean clickWidget(int parentId, int childId) {
        Widget widget = getWidget(parentId, childId);
<<<<<<< HEAD
        if (widget != null) {
            Microbot.getMouse().click(widget.getBounds());
            return true;
        }
        return false;
    }

    public static boolean clickWidget(WidgetInfo widgetInfo) {
        Widget widget = getWidget(widgetInfo);
        if (widget != null) {
            Microbot.getMouse().click(widget.getBounds());
            return true;
        }
        return false;
    }

    public static boolean isWidgetVisible(WidgetInfo wiget) {
        return !Microbot.getClientThread().runOnClientThread(() -> getWidget(wiget) == null || getWidget(wiget).isHidden());
=======
        return clickWidget(widget);
    }

    public static boolean isWidgetVisible(@Component int id) {
        Widget widget = getWidget(id);
        return !Microbot.getClientThread().runOnClientThread(() -> widget == null || widget.isHidden());
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    public static boolean isWidgetVisible(int widgetId, int childId) {
        return !Microbot.getClientThread().runOnClientThread(() -> getWidget(widgetId, childId) == null || getWidget(widgetId, childId).isHidden());
    }

<<<<<<< HEAD
    public static Widget getWidget(WidgetInfo wiget) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(wiget));
    }

    public static Widget getWidget(int id) {
=======
    public static Widget getWidget(@Component int id) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id));
    }

    public static boolean isHidden(int id) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return true;
            return widget.isHidden();
        });
    }


    public static Widget getWidget(int id, int child) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id, child));
    }

<<<<<<< HEAD
    public static boolean getWidgetChildText(int id, String matchingText) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return false;
            if (widget.getChildren().length == 0) return false;
            return Arrays.stream(widget.getChildren()).anyMatch(x -> x.getText().contains(matchingText));
        });
    }

    public static Widget getWidgetChildName(int id, String matchingText) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return null;
            if (widget.getChildren().length == 0) return null;
            return Arrays.stream(widget.getChildren()).filter(x -> x.getName().contains(matchingText)).findFirst().orElse(null);
        });
    }

    public static Widget getWidgetChildtxt(int id, String matchingText) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return null;
            if (widget.getChildren().length == 0) return null;
            return Arrays.stream(widget.getChildren()).filter(x -> x.getText().contains(matchingText)).findFirst().orElse(null);
        });
    }

    public static Widget getWidgetChildSprite(int id, int matchingSpriteId) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return null;
            if (widget.getChildren().length == 0) return null;
            return Arrays.stream(widget.getChildren()).filter(x -> x.getSpriteId() == (matchingSpriteId)).findFirst().orElse(null);
        });
    }

    public static int getChildWidgetSpriteID(int id, int childId) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id).getChild(childId).getSpriteId());
    }

    public static String getChildWidgetText(int id, int childId) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id).getChild(childId).getText());
=======
    public static int getChildWidgetSpriteID(int id, int childId) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id, childId).getSpriteId());
    }

    public static String getChildWidgetText(int id, int childId) {
        Widget widget = getWidget(id, childId);
        if (widget != null) {
            return widget.getText();
        }
        return "";
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    public static boolean clickWidget(int id) {
        Widget widget = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id));
        if (widget == null) return false;
        Microbot.getMouse().click(widget.getBounds());
        return true;
    }

    public static boolean clickChildWidget(int id, int childId) {
        Widget widget = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id));
        if (widget == null) return false;
        Microbot.getMouse().click(widget.getChild(childId).getBounds());
        return true;
    }

<<<<<<< HEAD
    public static boolean childWidgetExits(int id, int childId) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getWidget(id).getChild(childId) != null);
    }

    public static void changeWidgetText(String textToSearch, String newText) {
        do {
            try {
                Widget widget = findWidget(textToSearch, null);
                if (widget == null) break;
                Microbot.getClientThread().runOnClientThread(() -> widget.setText(newText));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        } while (true);
=======
    public static Widget findWidget(String text, List<Widget> children) {
        return findWidget(text, children, false);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    public static Widget findWidget(String text, List<Widget> children, boolean exact) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget foundWidget = null;
            if (children == null) {
                List<Widget> rootWidgets = Arrays.stream(Microbot.getClient().getWidgetRoots()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                for (Widget rootWidget : rootWidgets) {
                    if (exact) {
<<<<<<< HEAD
                        if (rootWidget.getText().toLowerCase().contains(text.toLowerCase()) || rootWidget.getName().toLowerCase().contains(">" + text.toLowerCase() + "<")) {
=======
                        String cleanText = rootWidget.getText() != null ? rootWidget.getText().replaceAll("<col=[^>]+>|</col>", "") : "";
                        String cleanName = rootWidget.getName() != null ? rootWidget.getName().replaceAll("<col=[^>]+>|</col>", "") : "";
                        if (cleanText.equalsIgnoreCase(text) || cleanName.equalsIgnoreCase(text)) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                            return rootWidget;
                        }
                    } else {
                        if (rootWidget.getText().toLowerCase().contains(text.toLowerCase()) || rootWidget.getName().toLowerCase().contains(text.toLowerCase())) {
                            return rootWidget;
                        }
                    }
                    if (rootWidget.getChildren() != null)
                        return findWidget(text, Arrays.stream(rootWidget.getChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
                    if (rootWidget.getNestedChildren().length > 0)
                        return findWidget(text, Arrays.stream(rootWidget.getNestedChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
                    if (rootWidget.getDynamicChildren().length > 0)
                        return findWidget(text, Arrays.stream(rootWidget.getDynamicChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
                    if (rootWidget.getStaticChildren().length > 0)
                        return findWidget(text, Arrays.stream(rootWidget.getStaticChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()), exact);
                }
            } else if (children.size() > 0) {
                for (Widget child : children) {
                    foundWidget = searchChildren(text, child, exact);
                    if (foundWidget != null) break;
                }
            }
            return foundWidget;
        });
    }

    public static Widget findWidget(String text) {
        return findWidget(text, null, false);
    }

<<<<<<< HEAD
    public static Widget findWidgetExact(String text) {
        return findWidget(text, null, true);
=======
    public static Widget findWidget(String text, boolean exact) {
        return findWidget(text, null, exact);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    public static boolean hasWidget(String text) {
        return findWidget(text, null, false) != null;
    }

    public static boolean sleepUntilHasWidget(String text) {
        sleepUntil(() -> findWidget(text, null, false) != null);
        return findWidget(text, null, false) != null;
    }

<<<<<<< HEAD
    public static Widget findWidget(String text, List<Widget> children) {
        return findWidget(text, children, false);
    }

=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

    public static Widget searchChildren(String text, Widget child, boolean exact) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget found = null;
            if (exact) {
<<<<<<< HEAD
                if (child.getText().toLowerCase().contains(text.toLowerCase()) || child.getName().toLowerCase().contains(">" + text.toLowerCase() + "<")) {
=======
                String cleanText = child.getText() != null ? child.getText().replaceAll("<col=[^>]+>|</col>", "") : "";
                String cleanName = child.getName() != null ? child.getName().replaceAll("<col=[^>]+>|</col>", "") : "";
                if (cleanText.equalsIgnoreCase(text) || cleanName.equalsIgnoreCase(text)) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                    return child;
                }
            } else {
                if (child.getText().toLowerCase().contains(text.toLowerCase()) || child.getName().toLowerCase().contains(text.toLowerCase())) {
                    return child;
                }
            }

            if (child.getChildren() != null) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(text, visibleChildWidgets, exact);
            }
            if (found != null) return found;
            if (child.getNestedChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getNestedChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(text, visibleChildWidgets, exact);
            }
            if (found != null) return found;
            if (child.getDynamicChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getDynamicChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(text, visibleChildWidgets, exact);
            }
            if (found != null) return found;
            if (child.getStaticChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getStaticChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(text, visibleChildWidgets, exact);
            }
            return found;
        });
    }

<<<<<<< HEAD
    public static Widget searchChildren(String text, Widget child) {
        return searchChildren(text, child, false);
    }

=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public static Widget findWidget(int spriteId, List<Widget> children) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget foundWidget = null;
            if (children == null) {
                List<Widget> rootWidgets = Arrays.stream(Microbot.getClient().getWidgetRoots()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                for (Widget rootWidget : rootWidgets) {
                    if (rootWidget.getSpriteId() == spriteId) {
                        return rootWidget;
                    }
                    if (rootWidget.getChildren() != null)
                        return findWidget(spriteId, Arrays.stream(rootWidget.getChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()));
                    if (rootWidget.getNestedChildren().length > 0)
                        return findWidget(spriteId, Arrays.stream(rootWidget.getNestedChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()));
                    if (rootWidget.getDynamicChildren().length > 0)
                        return findWidget(spriteId, Arrays.stream(rootWidget.getDynamicChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()));
                    if (rootWidget.getStaticChildren().length > 0)
                        return findWidget(spriteId, Arrays.stream(rootWidget.getStaticChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList()));
                }
            } else if (children.size() > 0) {
                for (Widget child : children) {
                    foundWidget = searchChildren(spriteId, child);
                    if (foundWidget != null) break;
                }
            }
            return foundWidget;
        });
    }

    public static Widget searchChildren(int spriteId, Widget child) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            Widget found = null;
            if (child.getSpriteId() == spriteId) {
                return child;
            }
            if (child.getChildren() != null) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(spriteId, visibleChildWidgets);
            }
            if (found != null) return found;
            if (child.getNestedChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getNestedChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(spriteId, visibleChildWidgets);
            }
            if (found != null) return found;
            if (child.getDynamicChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getDynamicChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(spriteId, visibleChildWidgets);
            }
            if (found != null) return found;
            if (child.getStaticChildren().length > 0) {
                List<Widget> visibleChildWidgets = Arrays.stream(child.getStaticChildren()).filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                if (visibleChildWidgets.size() > 0)
                    found = findWidget(spriteId, visibleChildWidgets);
            }
            return found;
        });
    }

    public static void clickWidgetFast(int packetId, int identifier) {
        Widget widget = getWidget(packetId);
        clickWidgetFast(widget, -1, identifier);
    }

<<<<<<< HEAD
    public static void clickWidgetFast(int packetId, MenuAction menuAction) {
        Widget widget = getWidget(packetId);
        clickWidgetFast(widget, menuAction);
    }

    public static void clickWidgetFast(int packetId) {
        Widget widget = getWidget(packetId, 1);
        clickWidgetFast(widget);
    }

    public static void clickWidgetFast(Widget widget, MenuAction menuAction) {
        int param1 = widget.getId();
        String option = "Select";
        String target = "";
        Microbot.doInvoke(new NewMenuEntry(-1, param1, menuAction.getId(), 0, widget.getItemId(), target), widget.getBounds());
        // Rs2Reflection.invokeMenu(-1, param1, menuAction.getId(), 0, widget.getItemId(), option, target, -1, -1);
    }

=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public static void clickWidgetFast(Widget widget, int param0, int identifier) {
        int param1 = widget.getId();
        String option = "Select";
        String target = "";
        MenuAction menuAction = MenuAction.CC_OP;
        Microbot.doInvoke(new NewMenuEntry(param0 != -1 ? param0 : widget.getType(), param1, menuAction.getId(), identifier, widget.getItemId(), target), widget.getBounds());
<<<<<<< HEAD
        // Rs2Reflection.invokeMenu(param0 != -1 ? param0 : widget.getType(), param1, menuAction.getId(), identifier, widget.getItemId(), option, target, -1, -1);
    }

    public static void clickWidgetFast(Widget widget, int param0, int identifier, MenuAction menuAction) {
        int param1 = widget.getId();
        String option = "Select";
        String target = "";
        Microbot.doInvoke(new NewMenuEntry(param0 != -1 ? param0 : widget.getType(), param1, menuAction.getId(), identifier, widget.getItemId(), target), widget.getBounds());
        // Rs2Reflection.invokeMenu(param0 != -1 ? param0 : widget.getType(), param1, menuAction.getId(), identifier, widget.getItemId(), option, target, -1, -1);
    }
=======
    }


>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

    public static void clickWidgetFast(Widget widget, int param0) {
        clickWidgetFast(widget, param0, 1);
    }

    public static void clickWidgetFast(Widget widget) {
        clickWidgetFast(widget, -1, 1);
    }

    // check if production widget is open
    public static boolean isProductionWidgetOpen() {
        return isWidgetVisible(270, 0);
    }

    // check if GoldCrafting widget is open
    public static boolean isGoldCraftingWidgetOpen() {
        return isWidgetVisible(446, 0);
    }

<<<<<<< HEAD
=======
    // check if SilverCrafting widget is open
    public static boolean isSilverCraftingWidgetOpen() {
        return isWidgetVisible(6, 0);
    }

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    // check if smithing widget is open
    public static boolean isSmithingWidgetOpen() {
        return isWidgetVisible(InterfaceID.SMITHING, 0);
    }
<<<<<<< HEAD
=======

    // check if deposit box widget is open
    public static boolean isDepositBoxWidgetOpen() {
        return isWidgetVisible(ComponentID.DEPOSIT_BOX_INVENTORY_ITEM_CONTAINER);
    }
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
}
