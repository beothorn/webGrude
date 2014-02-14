package webToJava.http;

import java.io.Serializable;
import java.util.Date;

import org.apache.http.cookie.Cookie;

public class SerializableCookie implements Cookie,Serializable {

	private static final long serialVersionUID = 1L;
	private final String name;
	private final String value;
	private final String comment;
	private final String commentUrl;
	private final Date expiryDate;
	private final boolean persistent;
	private final String domain;
	private final String path;
	private final int[] ports;
	private final boolean secure;
	private final int version;

	public SerializableCookie(final Cookie cookie) {
		name = cookie.getName();
		value = cookie.getValue();
		comment = cookie.getComment();
		commentUrl = cookie.getCommentURL();
		expiryDate = cookie.getExpiryDate();
		persistent = cookie.isPersistent();
		domain = cookie.getDomain();
		path = cookie.getPath();
		ports = cookie.getPorts();
		secure = cookie.isSecure();
		version = cookie.getVersion();
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getComment() {
		return comment;
	}

	public String getCommentURL() {
		return commentUrl;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public String getDomain() {
		return domain;
	}

	public String getPath() {
		return path;
	}

	public int[] getPorts() {
		return ports;
	}

	public boolean isSecure() {
		return secure;
	}

	public int getVersion() {
		return version;
	}

	public boolean isExpired(final Date date) {
		return date.after(expiryDate);
	}

}
