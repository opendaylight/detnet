/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.common.util;

import com.google.common.util.concurrent.Futures;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResultBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class RpcReturnUtil {
    private RpcReturnUtil() {

    }

    public static <T> Future<RpcResult<T>> returnErr(String errMsg) {
        return Futures.immediateFuture(RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg)
                .build());
    }

    public static <T> Future<RpcResult<T>> returnSucess(T out) {
        return Futures.immediateFuture(RpcResultBuilder.success(out).build());
    }

    public static ConfigureResult getConfigResult(boolean result, String errorCause) {
        ConfigureResultBuilder cfgResultBuilder  = new ConfigureResultBuilder();
        if (result) {
            cfgResultBuilder.setResult(ConfigureResult.Result.SUCCESS);
        } else {
            cfgResultBuilder.setResult(ConfigureResult.Result.FAILURE);
            cfgResultBuilder.setErrorCause(errorCause);
        }

        return cfgResultBuilder.build();
    }
}
