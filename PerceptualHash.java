package root.imager.hash.perceptual;

import java.awt.Color;
import java.awt.image.BufferedImage;
import root.imager.ImagePanel;
import root.imager.ImagerToolKit;

public class PerceptualHash 
{
	private ImagePanel imagePanel;
	private ImagerToolKit kit;
	
	public PerceptualHash(ImagePanel panel)
	{
		imagePanel = panel;
		kit = new ImagerToolKit(imagePanel.getToolkit());
	}
	
	private void processByPerceptualHash()
	{
		int x = 0;
		int y = 0;
		BufferedImage workImage = imagePanel.getNewImages().get(0);
		int width = workImage.getWidth();
		int height = workImage.getHeight();
		BufferedImage appropriateImage = null;
		
		boolean workProcess = true;
		
		BufferedImage tempImage = null;
		
		while (workProcess)
		{
			// Получение части основного изображения
			tempImage = imagePanel.getMainImage().getSubimage(x, y, width, height);
			// Получение изображение размерностью 8 на 8.
			double fw = 32 / (double) tempImage.getWidth();
			tempImage = kit.scale(tempImage, fw, fw);

			tempImage = kit.setImageColorToGray(tempImage);
			double averageBrightness = kit.getImageBrightness(tempImage);
			// Рассчитывает перцептивный хэш для части главного изображения
			int[][] perceptualHash = getPerceptualHash(tempImage, averageBrightness);
			
			
			int max = 0;
			for (int i = 0; i < imagePanel.getNewImages().size(); i++)
			{
				workImage = imagePanel.getNewImages().get(i);
				fw = 32 / (double) workImage.getWidth();
				workImage = kit.scale(workImage, fw, fw);
				workImage = kit.setImageColorToGray(workImage);
				double workAverageBrightness = kit.getImageBrightness(workImage);
				int[][] workPerceptualHash = getPerceptualHash(workImage, workAverageBrightness);
				
				int diff = comparePerceptualHashes(perceptualHash, workPerceptualHash);
				if (diff >= max)
				{
					max = diff;
					appropriateImage = imagePanel.getNewImages().get(i);
				}
			}
			System.out.println("max = " + max);
			
			
			
			if (x + width >= imagePanel.getMainImage().getWidth())
			{
				y = y + height;
				x = 0;
			}
			if (y + height > imagePanel.getMainImage().getHeight())
			{
				workProcess = false;
				break;
			}
			appropriateImage = kit.setImageColorToGray(appropriateImage);
			imagePanel.drawNewImage(appropriateImage, x, y);
			x = x + width;
			// Установка значения прогресс бара
//			progressBarValue++;
//			jProgressBar.setValue(progressBarValue);
		}
		
		kit.saveImageOnDisk(imagePanel.getMainImage(), 2011);
	}
	
	// Рассчитывает перцептуальный хэш - больше или меньше яркость пикселя относительно среднего значения
	// http://habrahabr.ru/post/120562/
	private int[][] getPerceptualHash(BufferedImage image, double averageBrightness)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] perceptualHash = new int[width][height];
		
		boolean hasalpha = image.getColorModel().hasAlpha();
		Color[][] raster = new Color[width][height];
		
		double brightness;
		
		for (int x = 0; x < width; x++)
		{			
			for (int y = 0; y < height; y++)
			{				
				raster[x][y] = new Color(image.getRGB(x, y), hasalpha);
				brightness = 0.3*raster[x][y].getRed() + 0.59*raster[x][y].getGreen() + 0.11*raster[x][y].getBlue();
				if (brightness >= averageBrightness)
				{
					perceptualHash[x][y] = 1;
				}
				else
				{
					perceptualHash[x][y] = 0;
				}
			}
		}
		return perceptualHash;
	}
		
	private int comparePerceptualHashes(int[][] perceptualHash, int[][] workPerceptualHash)
	{
		int result = 0;
		for (int i = 0; i < perceptualHash.length; i++)
		{
			for (int j = 0; j < perceptualHash[i].length; j++)
			{
				if (perceptualHash[i][j] == workPerceptualHash[i][j])
				{
					result++;
				}
			}
		}
		return result;
	}
}
