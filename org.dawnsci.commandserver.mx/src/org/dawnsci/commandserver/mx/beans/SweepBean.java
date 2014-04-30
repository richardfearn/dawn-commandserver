package org.dawnsci.commandserver.mx.beans;


/**
 * Bean holds information about a given data collections participation in
 * the auto-processing rerun.
 * 
 * @author fcp94556
 *
 */
public class SweepBean {

	private String        name;
	private String        dataCollectionId;
	private String        imageDirectory;
	private String        firstImageName;
	private int           start;
	private int           end;
	private double        wavelength = Double.NaN;
	private double        xBeam;
	private double        yBeam;
	
	public SweepBean() {
		
	}
	public SweepBean(String name, String dataCollectionId, int start, int end) {
		this.name             = name;
		this.dataCollectionId = dataCollectionId;
		this.start            = start;
		this.end              = end;
	}
	
	public String getDataCollectionId() {
		return dataCollectionId;
	}
	public void setDataCollectionId(String dataCollectionId) {
		this.dataCollectionId = dataCollectionId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dataCollectionId == null) ? 0 : dataCollectionId.hashCode());
		result = prime * result + end;
		result = prime * result
				+ ((imageDirectory == null) ? 0 : imageDirectory.hashCode());
		result = prime * result
				+ ((firstImageName == null) ? 0 : firstImageName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + start;
		long temp;
		temp = Double.doubleToLongBits(wavelength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xBeam);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yBeam);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SweepBean other = (SweepBean) obj;
		if (dataCollectionId == null) {
			if (other.dataCollectionId != null)
				return false;
		} else if (!dataCollectionId.equals(other.dataCollectionId))
			return false;
		if (end != other.end)
			return false;
		if (imageDirectory == null) {
			if (other.imageDirectory != null)
				return false;
		} else if (!imageDirectory.equals(other.imageDirectory))
			return false;
		if (firstImageName == null) {
			if (other.firstImageName != null)
				return false;
		} else if (!firstImageName.equals(other.firstImageName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (start != other.start)
			return false;
		if (Double.doubleToLongBits(wavelength) != Double
				.doubleToLongBits(other.wavelength))
			return false;
		if (Double.doubleToLongBits(xBeam) != Double
				.doubleToLongBits(other.xBeam))
			return false;
		if (Double.doubleToLongBits(yBeam) != Double
				.doubleToLongBits(other.yBeam))
			return false;
		return true;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageDirectory() {
		return imageDirectory;
	}
	public void setImageDirectory(String imageDirectory) {
		this.imageDirectory = imageDirectory;
	}
	public double getWavelength() {
		return wavelength;
	}
	public void setWavelength(double wavelength) {
		this.wavelength = wavelength;
	}
	public double getxBeam() {
		return xBeam;
	}
	public void setxBeam(double xBeam) {
		this.xBeam = xBeam;
	}
	public double getyBeam() {
		return yBeam;
	}
	public void setyBeam(double yBeam) {
		this.yBeam = yBeam;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getFirstImageName() {
		return firstImageName;
	}
	/**
	 * The image pattern is of the form:
	 *  <image prefix>_<data collection #>_${number}.<file extension>
	 *  
	 *  for instance:
	 *     JMJD2AA-x545_2_0001.cbf
	 * @param imageName
	 */
	public void setFirstImageName(String imageName) {
		this.firstImageName = imageName;
	}
}