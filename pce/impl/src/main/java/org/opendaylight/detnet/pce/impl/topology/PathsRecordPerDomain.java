/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.topology;

//import com.google.common.annotations.VisibleForTesting;
//import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.opendaylight.detnet.pce.impl.detnetpath.PathUnifyKey;
//import org.opendaylight.detnet.pce.impl.provider.PcePathImpl;



public final class PathsRecordPerDomain {
    private static PathsRecordPerDomain instance = new PathsRecordPerDomain();
    private Map<Integer, DomainRecord> domainRecords = new ConcurrentHashMap<Integer, DomainRecord>();
    //private PcePathImpl pcePathService;

    private PathsRecordPerDomain() {
    }

    public static PathsRecordPerDomain getInstance() {
        return instance;
    }

    /*
    public void setPcePathService(PcePathImpl pcePathService) {
        this.pcePathService = pcePathService;
    }*/

    public void add(Integer domainId, PathUnifyKey pathUnifyKey) {
        DomainRecord domainRecord = domainRecords.get(domainId);
        if (domainRecord == null) {
            synchronized (this) {
                domainRecord = domainRecords.get(domainId);
                /*
                if (domainRecord == null) {
                    domainRecord = new DomainRecord(domainId);
                    domainRecords.put(domainId, domainRecord);
                }*/
            }
        }

        domainRecord.add(pathUnifyKey);
    }

    public void remove(Integer domainId, PathUnifyKey pathUnifyKey) {
        DomainRecord domainRecord = domainRecords.get(domainId);
        if (domainRecord != null) {
            domainRecord.remove(pathUnifyKey);
        }
    }

    /*
    @VisibleForTesting
    public Set<PathUnifyKey> getPathSetByDomainId(Integer domainId) {
        DomainRecord domainRecord = domainRecords.get(domainId);
        if (domainRecord == null) {
            return new HashSet<PathUnifyKey>();
        }
        return domainRecord.getPathSet();
    }*/

    public void destroy() {
        domainRecords.clear();
    }


    private static class DomainRecord {
        //private Integer domainId;
        private Set<PathUnifyKey> pathSet = new ConcurrentHashSet<PathUnifyKey>();

        /*
        DomainRecord(Integer domainId) {
            this.domainId = domainId;
        }*/

        public void remove(PathUnifyKey tunnelUnifyKey) {
            pathSet.remove(tunnelUnifyKey);
        }

        public void add(PathUnifyKey tunnelUnifyKey) {
            pathSet.add(tunnelUnifyKey);
        }

        /*
        public Set<PathUnifyKey> getPathSet() {
            return pathSet;
        }*/
    }
}
