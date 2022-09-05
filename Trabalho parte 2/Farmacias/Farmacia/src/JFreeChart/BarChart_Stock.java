package JFreeChart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.ApplicationFrame;


public class BarChart_Stock extends ApplicationFrame {

	public BarChart_Stock(final String title, CategoryDataset dataset) {

		super(title);
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		chartPanel.setLocation(new Point(500,270));
		setContentPane(chartPanel);

	}

	private JFreeChart createChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createBarChart(
				"Stock das Farmácias",
		        "Medicamento",
		        "Número de Unidades",            
			    dataset,          
			    PlotOrientation.VERTICAL, 
			    true, true, false
		);
		
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		return chart;
	}

	public static void main(final String[] args, CategoryDataset dataset) {

		final BarChart_Stock barChart = new BarChart_Stock("Stock das Farmáias", dataset);
	       
   
	}
}