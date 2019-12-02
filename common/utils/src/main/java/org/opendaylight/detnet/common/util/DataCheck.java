/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.common.util;

public final class DataCheck {

    private DataCheck() {

    }

    public static CheckResult checkNotNull(Object... objs) {
        int index = 1;
        for (Object object : objs) {
            if (null == object) {
                return new CheckResult(false, String.valueOf(index));
            }
            index ++;
        }
        return new CheckResult(true, "");
    }

    public static class CheckResult {
        private boolean isIllegal;
        private String errorCause;

        public CheckResult(boolean isIllegal, String errorCause) {
            this.isIllegal = isIllegal;
            this.errorCause = errorCause;
        }

        public boolean isInputIllegal() {
            return isIllegal;
        }

        public String getErrorCause() {
            return errorCause;
        }
    }
}
