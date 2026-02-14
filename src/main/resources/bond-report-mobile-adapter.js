/* ========================================
   Bond Report Mobile Adaptation Layer (MINIMAL)
   ======================================== */

class MobileAdaptation {
    constructor() {
        this.isMobile = window.innerWidth <= 480;
        this.isTablet = window.innerWidth > 480 && window.innerWidth <= 1024;
        this.touchDevice = ('ontouchstart' in window);
        this.init();
    }

    init() {
        this.setupViewportMeta();
        this.addTouchOptimizations();
        this.logDeviceInfo();
    }

    setupViewportMeta() {
        let meta = document.querySelector('meta[name="viewport"]');
        if (!meta) {
            meta = document.createElement('meta');
            meta.name = 'viewport';
            document.head.appendChild(meta);
        }
        meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes, viewport-fit=cover');
    }

    addTouchOptimizations() {
        if (!this.touchDevice) return;

        const buttons = document.querySelectorAll('button, .btn');
        buttons.forEach(btn => {
            btn.style.minHeight = '44px';
            btn.style.minWidth = '44px';
        });

        const inputs = document.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.style.minHeight = '44px';
            input.style.fontSize = '16px';
        });
    }

    logDeviceInfo() {
        console.log('📱 Mobile Adaptation Initialized:', {
            isMobile: this.isMobile,
            isTablet: this.isTablet,
            isTouchDevice: this.touchDevice,
            viewportWidth: window.innerWidth
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.mobileAdapter = new MobileAdaptation();
});

const ResponsiveHelper = {
    isMobileDevice: () => window.innerWidth <= 480,
    isTabletDevice: () => window.innerWidth > 480 && window.innerWidth <= 1024,
    isTouchDevice: () => 'ontouchstart' in window
};