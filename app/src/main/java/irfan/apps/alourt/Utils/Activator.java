/*
 * Copyright (c) 2020 Irfan S.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Syed Irfan Ahmed <irfansa1@ymail.com>
 */

package irfan.apps.alourt.Utils;

public class Activator {
    long mobile;
    String location;
    String name;

    public Activator() {
    }

    public Activator(long mobile, String location, String name) {
        this.mobile = mobile;
        this.location = location;
        this.name = name;
    }

    public long getMobile() {
        return mobile;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
