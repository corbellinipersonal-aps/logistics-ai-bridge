const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');
const scenario = require('./scenario');

async function checkServer() {
    const { baseUrl } = scenario.config;
    console.log(`Checking if backend server is running on ${baseUrl} ...`);
    try {
        const response = await fetch(`${baseUrl}/`);
        if (!response.ok) return false;
        console.log('Server is up and running!');
    } catch (e) {
        return false;
    }

    console.log('Checking if "demo" Spring profile is active...');
    try {
        const demoCheck = await fetch(`${baseUrl}/api/demo/health`);
        if (demoCheck.status === 404) {
            console.error('\n❌ ERROR: /api/demo/health returned 404. The Spring "demo" profile is NOT active.');
            console.error('Start the server with: mvn spring-boot:run -Dspring-boot.run.profiles=demo');
            return false;
        }
    } catch (e) {
        return false;
    }
    console.log('Demo profile confirmed active.');
    return true;
}

(async () => {
    const isServerUp = await checkServer();
    if (!isServerUp) {
        process.exit(1);
    }

    console.log('Starting Playwright script using scenario-driven engine...');
    const browser = await chromium.launch({ headless: true });

    const videoDir = path.join(__dirname, 'videos');
    if (!fs.existsSync(videoDir)) {
        fs.mkdirSync(videoDir);
    }

    const { width, height, outputFileName } = scenario.config.video;

    const context = await browser.newContext({
        recordVideo: {
            dir: videoDir,
            size: { width, height }
        },
        viewport: { width, height }
    });

    const page = await context.newPage();

    // Accept dialogs automatically
    page.on('dialog', async dialog => {
        console.log(`Accepted dialog: ${dialog.message()}`);
        await dialog.accept();
    });

    try {
        for (const scene of scenario.scenes) {
            console.log(`--- Executing Scene: ${scene.id} (${scene.duration}s) ---`);
            const sceneStart = Date.now();
            
            // Execute scene actions
            if (scene.actions) {
                await scene.actions(page, scenario.config);
            }

            // Calculate remaining time to match scenario duration
            const elapsed = (Date.now() - sceneStart) / 1000;
            const remaining = scene.duration - elapsed;
            
            if (remaining > 0) {
                console.log(`Holding scene for ${remaining.toFixed(2)}s more...`);
                await page.waitForTimeout(remaining * 1000);
            } else {
                console.warn(`⚠️ Scene ${scene.id} took ${elapsed.toFixed(2)}s, exceeding duration of ${scene.duration}s!`);
            }
        }

        console.log('Finished all scenes. Closing context to save video...');
    } catch (error) {
        console.error('An error occurred during the script execution:', error);
        await page.screenshot({ path: path.join(videoDir, 'error-screenshot.png') });
    } finally {
        const video = page.video();
        await context.close(); 
        await browser.close();

        if (video) {
            const videoPath = await video.path();
            const targetPath = path.join(videoDir, outputFileName);
            if (fs.existsSync(targetPath)) {
                fs.unlinkSync(targetPath);
            }
            fs.renameSync(videoPath, targetPath);
            console.log(`Video file saved as ${outputFileName} in ${videoDir}`);
        }
    }
})();
