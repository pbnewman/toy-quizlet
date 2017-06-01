package edu.uwm.cs552.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * A component in which a font is chosen so as to fill up the
 * space give to this panel, up to margins.
 * Margins are specified with two numbers, one for the top and bottom margins
 * and one for the left and right margins.
 */
public class FlowFillingPanel extends JComponent {
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private float marginHeight, marginWidth;
	
	private final FlowComponent contents;
	
	/**
	 * Create a flow filling panel for the given flow component.
	 * @param fc flow component (must be a component).
	 */
	public FlowFillingPanel(FlowComponent fc) {
		contents = fc;
		super.add((JComponent)fc);
	}
	
	protected FlowComponent getContents() {
		return contents;
	}
	
	/**
	 * Set the gap at top and bottom of panel in line widths.
	 * @param h vertical size of the top and bottom margins (as a multiple of line height),
	 * which must be equal.
	 */
	public void setMarginHeight(float h) {
		marginHeight = h;
	}
	
	/**
	 * Set the gap at the left and the right of the panel.
	 * @param w horizontal size of the left and right margins (as a multiple of font size).
	 * which must be equal.
	 */
	public void setMarginWidth(float w) {
		marginWidth = w;
	}
	
	@Override
	public void doLayout() {
		Font f = getFont();
		
		int lo = 1;
		int hi = getWidth(); // much bigger than practical
		
		while (lo+1 < hi) {
			int mid = (lo+hi)/2;
			FontMetrics fm = getFontMetrics(f.deriveFont((float)mid));
			int width = getWidth() - Math.round(2*marginWidth*mid);
			int height = getHeight() - Math.round(2 * marginHeight * fm.getHeight());
			float h = contents.getPreferredHeight(fm,width);
			if (h <= height) lo = mid;
			else hi = mid;
		}
		
		final Font font = f.deriveFont((float)lo);
		FontMetrics fm = getFontMetrics(font);
		int width = getWidth() - Math.round(2*marginWidth*lo);
		int height = getHeight() - Math.round(2 * marginHeight * fm.getHeight());
		
		contents.setFont(font);
		contents.setBounds(new Rectangle(
				Math.round(marginWidth * lo),
				Math.round(marginHeight * fm.getHeight()),width,height));
	}

	public void dispose() {
		contents.dispose();
	}
	
	private static final String[] SAMPLE_STRINGS = 
		{"Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
		 "Ut purus elit, vestibulum ut, placerat ac, adipiscing vitae, felis.",
		 "Curabitur dictum gravida mauris.",
		 "Nam arcu libero, nonummy eget, consectetuer id, vulputate a, magna.", 
		 "Donec vehicula augue eu neque.", 
		 "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Mauris ut leo.", 
		 "Cras viverra metus rhoncus sem. "};
		
	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeLater(() -> {
			StackFlowComponent sf = new StackFlowComponent();
			for (String s : SAMPLE_STRINGS) {
				sf.add(new TextFlowComponent(s));
			}
			sf.setPadding(0.5f);
			FlowFillingPanel p = new FlowFillingPanel(sf);
			p.setMarginHeight(1);
			p.setMarginWidth(1);
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setTitle("Test Fill");
			f.setContentPane(p);
			f.setSize(500,300);
			f.setVisible(true);
		});
	}
}
