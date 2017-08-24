package org.proteored.pacom.utils;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;
import java.util.Set;

import javax.swing.JProgressBar;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

/**
 * Class for keeping component enable/disable status
 * 
 * @author Salva
 *
 */
public class ComponentEnableStateKeeper {
	private final Map<Component, Boolean> componentToEnableStateMap = new THashMap<Component, Boolean>();
	private final Set<Component> reversecomponents = new THashSet<Component>();
	private final Set<Component> invariableComponents = new THashSet<Component>();
	private boolean skipProgressBar;

	public ComponentEnableStateKeeper() {
		this(true);
	}

	public ComponentEnableStateKeeper(boolean skipProgressBar) {
		this.skipProgressBar = skipProgressBar;
	}

	private void addComponent(Component component) {
		componentToEnableStateMap.put(component, component.isEnabled());
	}

	private void addComponents(Component[] components) {
		for (Component component : components) {
			if (skipProgressBar && component instanceof JProgressBar) {
				continue;
			}
			if (invariableComponents.contains(component)) {
				continue;
			}
			componentToEnableStateMap.put(component, component.isEnabled());
		}
	}

	public void setToPreviousState(Container container) {
		synchronized (container.getTreeLock()) {
			if (!invariableComponents.contains(container) && componentToEnableStateMap.containsKey(container)) {
				container.setEnabled(componentToEnableStateMap.get(container));
			}
			Component[] components = container.getComponents();
			for (Component component : components) {
				if (component instanceof Container) {
					setToPreviousState((Container) component);
				}
			}
		}
	}

	public void enable(Container container) {
		setEnable(true, container);
	}

	public void disable(Container container) {
		setEnable(false, container);
	}

	private void setEnable(boolean b, Container container) {
		synchronized (container.getTreeLock()) {
			if (reversecomponents.contains(container)) {
				container.setEnabled(!b);
			} else if (componentToEnableStateMap.containsKey(container)) {
				container.setEnabled(b);
			}
			Component[] components = container.getComponents();
			for (Component component : components) {
				if (invariableComponents.contains(component)) {
					continue;
				}
				if (component instanceof Container) {
					setEnable(b, (Container) component);
				}
			}
		}

	}

	public void keepEnableStates(Container container) {
		componentToEnableStateMap.clear();
		keepEnableStatesPrivate(container);
	}

	private void keepEnableStatesPrivate(Container container) {
		synchronized (container.getTreeLock()) {
			Component[] components = container.getComponents();
			addComponents(components);
			for (Component component : components) {
				if (component instanceof Container) {
					keepEnableStatesPrivate((Container) component);
				}
			}
		}
	}

	/**
	 * Adds a component which will be disabled when calling enable() and will be
	 * disable when calling disable().<br>
	 * It will behave the same, keeping the previous state for
	 * updateEnableStates() and setPreviousState()
	 * 
	 * @param component
	 */
	public void addReverseComponent(Component component) {
		addComponent(component);
		this.reversecomponents.add(component);

	}

	/**
	 * component that will not be touched even if was added by one of its parent
	 * components
	 * 
	 * @param component
	 */
	public void addInvariableComponent(Component component) {
		this.invariableComponents.add(component);

	}
}
