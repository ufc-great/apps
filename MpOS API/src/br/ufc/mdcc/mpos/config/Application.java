package br.ufc.mdcc.mpos.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Tipo de Profile do aplicativo.
 * 
 * @author hack
 *
 */

@Documented
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Application {

	Profile profile() default Profile.DEFAULT;
	boolean deviceDetails() default false;
	boolean locationProfile() default false;
	boolean discoveryService() default true;
	
}
