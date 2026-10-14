document.addEventListener("DOMContentLoaded", () => {
    const cards = Array.from(document.querySelectorAll("[data-product-card]"));
    const filterButtons = Array.from(document.querySelectorAll("[data-filter]"));
    const emptyState = document.getElementById("emptyState");
    let activeFilter = "all";
    const queuePollingTokens = new Map();
    const QUEUE_POLL_INTERVAL = 1000;
    const QUEUE_POLL_TIMEOUT = 60000;

    function getSeckillFormState(form) {
        return form ? form.dataset.seckillState || "" : "";
    }

    function getSeckillStatusElement(form) {
        const nextElement = form.nextElementSibling;
        if (nextElement && nextElement.hasAttribute("data-seckill-status")) {
            return nextElement;
        }

        const statusElement = document.createElement("p");
        statusElement.className = "seckill-queue-status";
        statusElement.setAttribute("data-seckill-status", "");
        statusElement.setAttribute("role", "status");
        statusElement.setAttribute("aria-live", "polite");
        form.insertAdjacentElement("afterend", statusElement);
        return statusElement;
    }

    function updateSeckillStatus(form, message, state) {
        const statusElement = getSeckillStatusElement(form);
        statusElement.textContent = message;
        statusElement.className = "seckill-queue-status"
            + (state ? ` is-${state}` : "");
    }

    function updateSeckillButton(form, text, disabled) {
        const button = form.querySelector("button[type='submit']");
        if (!button) {
            return;
        }

        button.textContent = text;
        button.disabled = disabled;
    }

    function setSeckillState(form, state, message, buttonText, disabled) {
        form.dataset.seckillState = state;
        updateSeckillStatus(form, message, state);
        updateSeckillButton(form, buttonText, disabled);
    }

    function parseSeckillResultValue(data) {
        if (data && typeof data === "object" && data.orderId != null) {
            return Number(data.orderId);
        }
        if (data === null || data === "") {
            return Number.NaN;
        }
        return Number(data);
    }

    async function readJsonResponse(response) {
        const contentType = response.headers.get("content-type") || "";
        if (!contentType.includes("application/json")) {
            throw new Error("服务器未返回 JSON 结果");
        }
        return response.json();
    }

    async function requestSeckillPath(goodsId) {
        const pathUrl = new URL(
            "/seckill/path",
            window.location.origin
        );
        pathUrl.searchParams.set("goodsId", goodsId);

        const response = await fetch(pathUrl, {
            method: "GET",
            headers: {
                "Accept": "application/json"
            },
            cache: "no-store"
        });
        const result = await readJsonResponse(response);

        if (result.code !== 200) {
            const error = new Error(result.msg || "获取秒杀资格失败");
            error.code = result.code;
            throw error;
        }

        if (typeof result.data !== "string" || !result.data) {
            throw new Error("秒杀资格格式异常");
        }

        return result.data;
    }

    function completeSeckill(form, orderId) {
        queuePollingTokens.delete(form);
        setSeckillState(
            form,
            "success",
            `秒杀成功，订单号：${orderId}`,
            "秒杀成功",
            true
        );
    }

    function failSeckill(form, message) {
        queuePollingTokens.delete(form);
        setSeckillState(form, "failed", message, "秒杀失败", true);
    }

    function stopQueuePolling(form, message, buttonText = "查询超时") {
        queuePollingTokens.delete(form);
        setSeckillState(form, "failed", message, buttonText, true);
    }

    function startQueuePolling(form, goodsId) {
        const token = Symbol("seckill-queue");
        const startedAt = Date.now();
        queuePollingTokens.set(form, token);
        setSeckillState(form, "queue", "正在排队，请稍候...", "排队中", true);

        const poll = async () => {
            if (queuePollingTokens.get(form) !== token) {
                return;
            }

            const elapsed = Date.now() - startedAt;
            if (elapsed >= QUEUE_POLL_TIMEOUT) {
                stopQueuePolling(form, "排队处理超时，请稍后重新查询");
                return;
            }

            const resultUrl = new URL(
                "/seckill-order/result",
                window.location.origin
            );
            resultUrl.searchParams.set("goodsId", goodsId);

            try {
                const response = await fetch(resultUrl, {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    },
                    cache: "no-store"
                });

                if (response.status === 404) {
                    stopQueuePolling(
                        form,
                        "结果查询接口暂不可用，请刷新页面后查看",
                        "结果未确认"
                    );
                    return;
                }

                const result = await readJsonResponse(response);
                if (result.code !== 200) {
                    if (result.code === 500100 || result.code === 500202) {
                        window.location.href = "/tologin";
                        return;
                    }
                    failSeckill(form, result.msg || "秒杀失败");
                    return;
                }

                const resultValue = parseSeckillResultValue(result.data);
                if (resultValue === 0) {
                    const seconds = Math.floor(elapsed / 1000);
                    updateSeckillStatus(
                        form,
                        `正在排队，请稍候... 已等待 ${seconds} 秒`,
                        "queue"
                    );
                } else if (resultValue > 0) {
                    completeSeckill(form, resultValue);
                    return;
                } else if (resultValue === -1) {
                    failSeckill(form, "秒杀失败，库存不足或未抢到");
                    return;
                } else {
                    failSeckill(form, "秒杀结果异常，请稍后重试");
                    return;
                }
            } catch (error) {
                if (queuePollingTokens.get(form) !== token) {
                    return;
                }
                updateSeckillStatus(form, "排队结果查询中，网络波动不会影响请求", "queue");
            }

            if (queuePollingTokens.get(form) === token) {
                window.setTimeout(poll, QUEUE_POLL_INTERVAL);
            }
        };

        window.setTimeout(poll, 800);
    }

    async function submitSeckill(form) {
        const goodsId = form.elements.goodsid
            ? form.elements.goodsid.value
            : "";

        if (!goodsId) {
            failSeckill(form, "商品信息缺失，请刷新页面后重试");
            return;
        }

        setSeckillState(form, "submitting", "正在获取秒杀资格...", "获取中", true);

        try {
            const path = await requestSeckillPath(goodsId);
            const requestBody = new URLSearchParams(new FormData(form));
            requestBody.set("path", path);

            setSeckillState(
                form,
                "submitting",
                "正在提交秒杀请求...",
                "提交中",
                true
            );

            const response = await fetch(form.action, {
                method: "POST",
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: requestBody
            });
            const result = await readJsonResponse(response);

            if (result.code !== 200) {
                if (result.code === 500100 || result.code === 500202) {
                    window.location.href = "/tologin";
                    return;
                }
                failSeckill(form, result.msg || "秒杀失败");
                return;
            }

            const resultValue = parseSeckillResultValue(result.data);
            if (resultValue === 0) {
                startQueuePolling(form, goodsId);
                return;
            }
            if (resultValue > 0) {
                completeSeckill(form, resultValue);
                return;
            }
            if (resultValue === -1) {
                failSeckill(form, "秒杀失败，库存不足或未抢到");
                return;
            }

            failSeckill(form, "秒杀结果异常，请稍后重试");
        } catch (error) {
            if (error.code === 500100 || error.code === 500202) {
                window.location.href = "/tologin";
                return;
            }
            failSeckill(
                form,
                error.message || "网络异常或服务响应异常，请稍后重试"
            );
        }
    }

    function parseDate(value) {
        if (!value) {
            return null;
        }
        const date = new Date(String(value).replace(" ", "T"));
        return Number.isNaN(date.getTime()) ? null : date;
    }

    function pad(value) {
        return String(value).padStart(2, "0");
    }

    function formatDuration(milliseconds) {
        const totalSeconds = Math.max(0, Math.floor(milliseconds / 1000));
        const days = Math.floor(totalSeconds / 86400);
        const hours = Math.floor((totalSeconds % 86400) / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        if (days > 0) {
            return `${days}天 ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
        }
        return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
    }

    function resolveStatus(startTime, endTime, stockCount, now) {
        if (Number(stockCount) <= 0) {
            return "sold";
        }
        if (startTime && now < startTime.getTime()) {
            return "upcoming";
        }
        if (endTime && now >= endTime.getTime()) {
            return "ended";
        }
        return "live";
    }

    function statusText(status) {
        return {
            live: "立即抢购",
            upcoming: "即将开始",
            ended: "活动已结束",
            sold: "已售罄"
        }[status];
    }

    function applyFilter() {
        let visibleCount = 0;

        cards.forEach((card) => {
            const visible = activeFilter === "all"
                || card.dataset.status === activeFilter
                || (activeFilter === "ended" && card.dataset.status === "sold");

            card.hidden = !visible;
            if (visible) {
                visibleCount += 1;
            }
        });

        filterButtons.forEach((button) => {
            button.classList.toggle("is-active", button.dataset.filter === activeFilter);
        });

        if (emptyState) {
            emptyState.style.display = cards.length === 0 || visibleCount === 0 ? "block" : "none";
        }
    }

    function updateCards(now) {
        let globalTarget = null;
        let globalLabel = "本场倒计时";

        cards.forEach((card) => {
            const startTime = parseDate(card.dataset.start);
            const endTime = parseDate(card.dataset.end);
            const stockCount = Number(card.dataset.stock || 0);
            const status = resolveStatus(startTime, endTime, stockCount, now);
            const statusBadge = card.querySelector("[data-status-label]");
            const countdownLabel = card.querySelector("[data-countdown-label]");
            const countdownValue = card.querySelector("[data-countdown-value]");
            const buyButton = card.querySelector("[data-buy-button]");
            const buyForm = card.querySelector(".buy-form");
            let targetTime = null;

            card.dataset.status = status;
            if (statusBadge) {
                statusBadge.textContent = statusText(status);
            }
            if (buyButton && !getSeckillFormState(buyForm)) {
                buyButton.textContent = statusText(status);
                buyButton.disabled = status !== "live";
            }

            if (status === "upcoming") {
                targetTime = startTime;
                if (countdownLabel) {
                    countdownLabel.textContent = "距开始";
                }
            } else if (status === "live") {
                targetTime = endTime;
                if (countdownLabel) {
                    countdownLabel.textContent = "距结束";
                }
            } else if (countdownLabel && countdownValue) {
                countdownLabel.textContent = status === "sold" ? "库存状态" : "场次状态";
                countdownValue.textContent = statusText(status);
            }

            if (targetTime && countdownValue) {
                countdownValue.textContent = formatDuration(targetTime.getTime() - now);
                if (!globalTarget || targetTime.getTime() < globalTarget.getTime()) {
                    globalTarget = targetTime;
                    globalLabel = status === "live" ? "本场倒计时" : "距下一场";
                }
            }
        });

        const globalLabelElement = document.getElementById("globalTimerLabel");
        const globalValueElement = document.getElementById("globalTimerValue");
        if (globalLabelElement && globalValueElement) {
            if (globalTarget) {
                globalLabelElement.textContent = globalLabel;
                globalValueElement.textContent = formatDuration(globalTarget.getTime() - now);
            } else {
                globalLabelElement.textContent = "本场状态";
                globalValueElement.textContent = cards.length > 0 ? "活动已结束" : "等待上架";
            }
        }
    }

    function updateDetail(now) {
        const detail = document.querySelector("[data-countdown]");
        if (!detail) {
            return;
        }

        const startTime = parseDate(detail.dataset.start);
        const endTime = parseDate(detail.dataset.end);
        const stockCount = Number(detail.dataset.stock || 0);
        const status = resolveStatus(startTime, endTime, stockCount, now);
        const targetTime = status === "upcoming" ? startTime : endTime;
        const label = document.querySelector("[data-detail-countdown-label]");
        const value = document.querySelector("[data-detail-countdown-value]");
        const buyButton = document.querySelector("[data-detail-buy-button]");
        const buyForm = document.querySelector(".detail-actions .buy-form");

        if (label) {
            label.textContent = status === "upcoming"
                ? "距开始"
                : status === "live" ? "距结束" : "当前状态";
        }
        if (value) {
            value.textContent = targetTime && (status === "upcoming" || status === "live")
                ? formatDuration(targetTime.getTime() - now)
                : statusText(status);
        }
        if (buyButton && !getSeckillFormState(buyForm)) {
            buyButton.textContent = statusText(status);
            buyButton.disabled = status !== "live";
        }
    }

    function tick() {
        const now = Date.now();
        updateCards(now);
        updateDetail(now);
    }

    filterButtons.forEach((button) => {
        button.addEventListener("click", () => {
            activeFilter = button.dataset.filter;
            applyFilter();
        });
    });

    document.querySelectorAll(".buy-form").forEach((form) => {
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            submitSeckill(form);
        });
    });

    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        const message = document.getElementById("loginMessage");
        const submitButton = loginForm.querySelector("button[type='submit']");
        let captchaPassed = false;
        let captchaLoading = false;
        let captchaInstance = null;

        async function submitLogin() {
            submitButton.disabled = true;
            message.textContent = "";

            try {
                const response = await fetch(loginForm.action, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    },
                    body: new URLSearchParams(new FormData(loginForm))
                });
                const result = await response.json();
                if (result.code === 200) {
                    window.location.href = "/goods/toDetail";
                    return;
                }

                captchaPassed = false;
                message.textContent = result.msg || "登录失败";
            } catch (error) {
                captchaPassed = false;
                message.textContent = "网络异常，请稍后重试";
            } finally {
                submitButton.disabled = false;
            }
        }

        function openLoginCaptcha() {
            if (captchaInstance
                    || captchaLoading
                    || document.querySelector("#tianai-captcha-parent")) {
                return;
            }
            if (typeof window.initTAC !== "function") {
                message.textContent = "验证码加载失败，请刷新页面";
                return;
            }

            captchaLoading = true;
            message.textContent = "请先完成滑块验证";

            window.initTAC("/tac", {
                requestCaptchaDataUrl: "/captcha/gen",
                validCaptchaUrl: "/captcha/check",
                bindEl: "#captcha-box",
                validSuccess: (res, captcha, tac) => {
                    captchaInstance = null;
                    captchaPassed = true;
                    tac.destroyWindow();
                    submitLogin();
                },
                validFail: (res, captcha, tac) => {
                    tac.reloadCaptcha();
                },
                btnRefreshFun: (element, tac) => {
                    tac.reloadCaptcha();
                },
                btnCloseFun: (element, tac) => {
                    captchaInstance = null;
                    tac.destroyWindow();
                    message.textContent = "";
                }
            }, {
                logoUrl: null
            }).then((tac) => {
                captchaLoading = false;
                captchaInstance = tac;
                tac.init();
            }).catch(() => {
                captchaLoading = false;
                captchaInstance = null;
                message.textContent = "验证码加载失败，请刷新页面";
            });
        }

        loginForm.addEventListener("submit", (event) => {
            event.preventDefault();

            if (!captchaPassed) {
                openLoginCaptcha();
                return;
            }

            submitLogin();
        });
    }

    const settingsForms = Array.from(document.querySelectorAll("[data-settings-form]"));
    settingsForms.forEach((form) => {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const message = form.querySelector("[data-form-message]");
            const submitButton = form.querySelector("button[type='submit']");
            const phoneInput = form.elements.id;
            const passwordInput = form.elements.password;
            const confirmInput = form.querySelector("[data-confirm-for]");
            let succeeded = false;

            message.classList.remove("is-success");
            message.textContent = "";

            if (phoneInput && !/^1[3-9]\d{9}$/.test(phoneInput.value.trim())) {
                message.textContent = "请输入正确的11位手机号";
                phoneInput.focus();
                return;
            }

            if (passwordInput && confirmInput
                    && passwordInput.value !== confirmInput.value) {
                message.textContent = "两次输入的新密码不一致";
                confirmInput.focus();
                return;
            }

            submitButton.disabled = true;

            try {
                const response = await fetch(form.action, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    },
                    body: new URLSearchParams(new FormData(form))
                });
                const result = await response.json();

                if (result.code === 200) {
                    succeeded = true;
                    message.classList.add("is-success");
                    message.textContent = "修改成功，即将重新登录";
                    form.querySelectorAll("input").forEach((input) => {
                        input.disabled = true;
                    });
                    window.setTimeout(() => {
                        window.location.href = "/tologin";
                    }, 900);
                    return;
                }

                message.textContent = result.msg || "修改失败";
            } catch (error) {
                message.textContent = "网络异常，请稍后重试";
            } finally {
                if (!succeeded) {
                    submitButton.disabled = false;
                }
            }
        });
    });

    function initUserHeader() {
        const actions = document.querySelector("[data-user-actions]");
        if (!actions) {
            return;
        }

        const settingsLink = actions.querySelector("[data-settings-link]");
        const nameChip = actions.querySelector("[data-user-name]");
        const loginLink = actions.querySelector("[data-login-link]");

        fetch("/user/current", {
            headers: {"Accept": "application/json"},
            cache: "no-store"
        })
            .then((response) => response.json())
            .then((result) => {
                if (result.code !== 200) {
                    return;
                }

                const username = result.data;
                if (typeof username !== "string" || !username) {
                    return;
                }

                if (settingsLink) {
                    settingsLink.hidden = false;
                }
                if (nameChip) {
                    nameChip.textContent = username;
                    nameChip.hidden = false;
                }
                if (loginLink) {
                    loginLink.hidden = true;
                }
            })
            .catch(() => {
                // 登录态获取失败时保持未登录样式，不阻塞页面主流程
            });
    }

    tick();
    applyFilter();
    initUserHeader();
    window.setInterval(tick, 1000);
});
