// presentation/start_cloud_tunnel.js
// Cloud tunnel daemon providing persistent public cloud access to DJ Mart

const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const https = require('https');

const statusFile = path.join(__dirname, '..', 'cloud_deployment.json');

function log(msg) {
  const line = `[${new Date().toISOString()}] ${msg}`;
  console.log(line);
  fs.appendFileSync(path.join(__dirname, '..', 'cloud_tunnel.log'), line + '\n');
}

// Fetch public IP for visitor convenience
function getPublicIp() {
  return new Promise(resolve => {
    https.get('https://api.ipify.org', res => {
      let data = '';
      res.on('data', d => data += d);
      res.on('end', () => resolve(data.trim()));
    }).on('error', () => resolve('223.236.152.71'));
  });
}

async function startTunnel() {
  const publicIp = await getPublicIp();
  log(`Detected Host Public IP: ${publicIp}`);

  log('Starting localtunnel on port 8081 with subdomain: djmart-online...');
  const lt = spawn('cmd.exe', ['/c', 'npx', 'localtunnel', '--port', '8081', '--subdomain', 'djmart-online']);

  let urlFound = false;

  lt.stdout.on('data', d => {
    const text = d.toString();
    log(`Tunnel STDOUT: ${text}`);

    const match = text.match(/your url is: (https:\/\/[^\s]+)/i);
    if (match) {
      const publicUrl = match[1];
      urlFound = true;
      log(`Public Cloud Deployment Live at: ${publicUrl}`);

      const info = {
        deploymentStatus: 'LIVE',
        publicUrl: publicUrl,
        endpointIpPassword: publicIp,
        localPort: 8081,
        rootContext: '/',
        djMartContext: '/Dj Mart/',
        cloudPlatform: 'Localtunnel Public Cloud Gateway',
        lastUpdated: new Date().toISOString(),
        instructions: 'First time browser visitors: click "Click to Continue" or enter Endpoint IP: ' + publicIp
      };

      fs.writeFileSync(statusFile, JSON.stringify(info, null, 2), 'utf8');
    }
  });

  lt.stderr.on('data', d => {
    log(`Tunnel STDERR: ${d.toString()}`);
  });

  lt.on('close', code => {
    log(`Tunnel process exited with code ${code}. Reconnecting in 3 seconds...`);
    setTimeout(startTunnel, 3000);
  });
}

startTunnel();
