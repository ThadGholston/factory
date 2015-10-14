/*
* File:   KarthikEvent.cpp
* Author: Dustin
*
* Created on January 16, 2014, 4:06 PM
*/
#ifndef POINTCONVERTER_CPP
#define POINTCONVERTER_CPP

#ifndef M_PI
#define M_PI 3.14159265358979323846264338327
#endif

#include <math.h>
#include "../include/IPointConverter.hpp"

class PointConverter: public IPointConverter{
private:
	static constexpr double RADTODEG = 180 / M_PI;
	static constexpr double CDELT = 0.599733;
	static constexpr double HPCCENTER = 4096.0 / 2.0;

	static constexpr double rsun_meters = 696000;
	static constexpr double dsun_meters = 149600000;

	double rad2deg(double val) {
		double returnVal = val * this->RADTODEG;
		return returnVal;
	}

	double deg2rad(double val) {
		double returnVal = val / this->RADTODEG;
		return returnVal;
	}

public:

	/**
	* convertToPixXY   :converts a point in HPC coordinates to Pixel XY coordinates
	* @param pointIn   :point to convert
	* @return          :a new point in Pixel XY coordinates
	*/
	Point2i convertHPCToPixXY(Point2d pointIn){
		double PixX = (HPCCENTER + (pointIn.x / CDELT));
		double PixY = (HPCCENTER - (pointIn.y / CDELT));
		return Point2i(PixX, PixY);
	}

	/**
	* convertToPixXY   :converts a point in HGS coordinates to Pixel XY coordinates
	* @param pointIn   :point to convert
	* @return          :a new point in Pixel XY coordinates
	*/
	Point2i convertHGSToPixXY(Point2d pointIn){
		Point2d pointHCC = this->convertHG_HCC(pointIn.x, pointIn.y);
		Point2d pointHPC = this->convertHCC_HPC(pointHCC.x, pointHCC.y);

		double PixX = (HPCCENTER + (pointHPC.x / CDELT));
		double PixY = (HPCCENTER - (pointHPC.y / CDELT));
		return Point2i(PixX, PixY);
	}


	/**
	* convertToHGS     :converts a point in Pixel XY coordinates to HGS coordinates
	* @param pointIn   :the point to convert
	* @return          :a new point in HGS
	*/
	Point2d convertPixXYToHGS(Point2i pointIn){
		double HPCx = (pointIn.x - this->HPCCENTER) * CDELT;
		double HPCy = (HPCCENTER - pointIn.y) * CDELT;

		Point2d hccPoint = this->convertHPC_HCC(HPCx, HPCy);
		Point2d hgsPoint = this->convertHCC_HG(hccPoint.x, hccPoint.y);
		
		return hgsPoint;
	}


	/**
	* convertToHGS     :converts a point in Pixel XY coordinates to HPC coordinates
	* @param pointIn   :the point to convert
	* @return          :a new point in HPC
	*/
	Point2d convertPixXYToHPC(Point2i pointIn){
		double HPCx = (pointIn.x - this->HPCCENTER) * CDELT;
		double HPCy = (HPCCENTER - pointIn.y) * CDELT;

		Point2d ptd(HPCx, HPCy);
		return ptd;
	}

	/************************************************************************/
	    //    """Convert from Heliographic coordinates (given in degrees) to
		//    Heliocentric - Cartesian coordinates(given in meters)
		//
		//    Parameters
		//            ----------
		//            hglon_deg, hglat_deg : float (degrees)
		//            Heliographic longitude and latitude in degrees.
		//            b0_deg : float (degrees)
		//            Tilt of the solar North rotational axis toward the observer
		//            (heliographic latitude of the observer). Usually given as SOLAR_B0,
		//            HGLT_OBS, or CRLT_OBS. Default is 0.
		//            l0_deg : float (degrees)
		//            Carrington longitude of central meridian as seen from Earth. Default is 0.
		//            occultation : Bool
		//            If true set all points behind the Sun(e.g. not visible) to Nan.
		//            z : Bool
		//            If true return the z coordinate as well.
		//            r : float (meters)
		//            Heliographic radius
		//
		//            Returns
		//            -------
		//            out : ndarray(meters)
		//            The data coordinates in Heliocentric - Cartesian coordinates.
		//
		//            Notes
		//            -----
		//            Implements Eq.(11) of Thompson(2006), A&A, 449, 791, with the default
		//    assumption that the value 'r' in Eq. (11) is identical to the radius of the
		//    Sun.
		//
		//            Examples
		//            --------
		//            >> > sunpy.wcs.convert_hg_hcc(0.01873188196651189, 3.6599471896203317,
		//            r = 704945784.41465974, z = True)
		//            (230000.0, 45000000.0, 703508000.0)
		//            """ 
	/************************************************************************/
	Point2d convertHG_HCC(double hglon_deg, double hglat_deg){
	
		double b0_deg = 0;
		double l0_deg = 0;
		double lon = deg2rad(hglon_deg);
		double lat = deg2rad(hglat_deg);

		double cosb = cos(deg2rad(b0_deg));
		double sinb = sin(deg2rad(b0_deg));

		lon = lon - deg2rad(l0_deg);

		double cosx = cos(lon);
		double sinx = sin(lon);
		double cosy = cos(lat);
		double siny = sin(lat);

		//#Perform the conversion.
		double x = this->rsun_meters * cosy * sinx;
		double y = this->rsun_meters * (siny * cosb - cosy * cosx * sinb);
		//        double zz = r * (siny * sinb + cosy * cosx * cosb);


		return Point2d(x, y);
	}

	/************************************************************************/
		//    """Converts from Helioprojective-Cartesian (HPC) coordinates into
		//    Heliocentric-Cartesian (HCC) coordinates. Returns all three dimensions, x, y, z in
		//    meters.
		//
		//    Parameters
		//    ----------
		//    x, y : float
		//        Data coordinate in angle units (default is arcsec)
		//    dsun_meters : float
		//        Distance from the observer to the Sun in meters. Default is 1 AU.
		//    angle_units : str
		//        Units of the data coordinates (e.g. arcsec, arcmin, degrees). Default is arcsec.
		//    z : Bool
		//        If true return the z coordinate as well.
		//
		//    Returns
		//    -------
		//    out : ndarray
		//        The  data coordinates (x,y,z) in heliocentric cartesian coordinates in meters.
	/************************************************************************/
	Point2d convertHPC_HCC(double x, double y){

		double arcSec = deg2rad(1) / (60 * 60);
		double cosx = cos(x * arcSec);
		double sinx = sin(x * arcSec);
		double cosy = cos(y * arcSec);
		double siny = sin(y * arcSec);



		double q = dsun_meters * cosy * cosx;
		double distance = pow(q, 2.0) - pow(dsun_meters, 2.0) + pow(rsun_meters, 2.0);

		if (distance < 0) {
			std::complex<double> mycomplex(distance);
			mycomplex = std::sqrt(mycomplex);
			distance = q - mycomplex.real();
		}
		else {
			distance = q - sqrt(distance);
		}

		double rx = distance * cosy * sinx;
		double ry = distance * siny;

		return Point2d(rx, ry);
	}

	/************************************************************************/
		//    """Convert Heliocentric-Cartesian (HCC) to angular
		//    Helioprojective-Cartesian (HPC) coordinates (in degrees).
		//
		//    Parameters
		//    ----------
		//    x, y : float (meters)
		//        Data coordinate in meters.
		//    dsun_meters : float
		//        Distance from the observer to the Sun in meters. Default is 1 AU.
		//    angle_units : str
		//        Units of the data coordinates (e.g. arcsec, arcmin, degrees). Default is arcsec.
		//
		//    Returns
		//    -------
		//    out : ndarray
		//        The  data coordinates (x,y) in helioprojective cartesian coordinates in arcsec.
		//
		//    Notes
		//    -----
		//
		//    Examples
		//    --------
		//
		//    """
		//    # Calculate the z coordinate by assuming that it is on the surface of the Sun                                                                     */
	/************************************************************************/
	Point2d convertHCC_HPC(double x, double y){
		double z = sqrt(pow(rsun_meters, 2) - pow(x, 2) - pow(y, 2));


		double zeta = dsun_meters - z;
		double distance = sqrt(pow(x, 2) + pow(y, 2) + pow(zeta, 2));
		double hpcx = this->rad2deg(atan2(x, zeta));
		double hpcy = this->rad2deg(asin(y / distance));

		//        if angle_units == 'arcsec' :
		hpcx = 60 * 60 * hpcx;
		hpcy = 60 * 60 * hpcy;


		return Point2d(hpcx, hpcy);
	}


	/************************************************************************/
		//    """Convert from Heliocentric-Cartesian (HCC) (given in meters) to
		//    Heliographic coordinates (HG) given in degrees, with radial output in
		//    meters.
		//
		//    Parameters
		//    ----------
		//    x, y : float (meters)
		//        Data coordinate in meters.
		//    z : float (meters)
		//        Data coordinate in meters.  If None, then the z-coordinate is assumed
		//        to be on the Sun.
		//    b0_deg : float (degrees)
		//        Tilt of the solar North rotational axis toward the observer
		//        (heliographic latitude of the observer). Usually given as SOLAR_B0,
		//        HGLT_OBS, or CRLT_OBS. Default is 0.
		//    l0_deg : float (degrees)
		//        Carrington longitude of central meridian as seen from Earth. Default is 0.
		//    radius : Bool
		//        If true, forces the output to return a triple of (lon, lat, r). If
		//        false, return (lon, lat) only.
		//
		//    Returns
		//    -------
		//    out : ndarray (degrees, meters)
		//        if radius is false, return the data coordinates (lon, lat).  If
		//        radius=True, return the data cordinates (lon, lat, r).  The quantities
		//        (lon, lat) are the heliographic coordinates in degrees.  The quantity
		//        'r' is the heliographic radius in meters.
	/************************************************************************/
	Point2d convertHCC_HG(double x, double y){

		float b0_deg = 0;
		float l0_deg = 0;

		double d = pow(rsun_meters, 2) - pow(x, 2) - pow(y, 2);
		double z;
		if (d < 0) {
			z = 0;
		}
		else {
			z = sqrt(d);
		}


		double cosb = cos(deg2rad(b0_deg));
		double sinb = sin(deg2rad(b0_deg));

		double hecr = sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
		double hgln = atan2(x, z * cosb - y * sinb) + deg2rad(l0_deg);
		double hglt = asin((y * cosb + z * sinb) / hecr);


		return Point2d(rad2deg(hgln), rad2deg(hglt));
	}
};

#endif /*POINTCONVERTER_CPP*/