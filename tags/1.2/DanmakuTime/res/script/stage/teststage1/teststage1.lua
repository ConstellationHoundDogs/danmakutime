
local texpfx = "stage/teststage1/enemy.png#"

-------------------------------------------------------------------------------

local function moveTo(sprite, targetX, targetY, frames)
    frames = frames or 0

    for i=1,frames do
        local d = sprite:getSpeed() + 1
        local dy = targetY-sprite:getY()
        local dx = targetX-sprite:getX()
        if math.abs(dx) <= d and math.abs(dy) <= d then
            return true
        end
        
        sprite:setAngle(math.atan2(dy, dx))
        yield()
    end
    return false
end

-------------------------------------------------------------------------------

local BaseEnemy = {
    }

function BaseEnemy.new(self)
	self = extend(BaseEnemy, self or {})
	self = THSprite.new(self)
    
    self:setTexture(texStore:get(texpfx .. "enemy0"))
    self:setColNode(0, enemyColType, CircleColNode.new(12))
    self:setAngle(256)
    self:setDrawAngleAuto(false)
    
    return self
end

-------------------------------------------------------------------------------

local BaseShot = {}

function BaseShot.new(owner, self)
	self = extend(BaseShot, self or {})
	self = THShot.new(owner, self)
    
    self:setTexture(texStore:get(texpfx .. "shotRed"))
    self:setColNode(0, enemyShotColType, CircleColNode.new(6))
    self:setSpeed(3)
    self:setDrawAngleAuto(false)
    
    return self
end

-------------------------------------------------------------------------------

local function enemy0(x, y, a, sx, sy)
    local x = x or levelWidth/2
    local y = y or -16
    local a = a or 0
    local sx = sx or .5
    local sy = sy or .25
    
    local e = BaseEnemy.new{hp=250}
    e:setPos(x, y)
    e:setSpeed(1)
    
    e.fire = function(self, a)
        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setAngle(a)

        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setAngle(a + 256)
    end
    
    e.fireThread = function(self)
        while self:getY() < levelHeight*.8 do
            local a2 = a % 128
            if (a2 > 64-4 and a2 < 64+4) then
                --Do nothing
            else
                self:fire(a)
            end
        
            a = (a + 1) % 512
            yield(2)
        end
        self:setSpeedInc(.05)    
    end
    
    e.update = function(self)
        yield(60)
        self:setSpeed(0)
        
        self:addThread(self.fireThread)
        
        local x0 = self:getX()
        local t = 0
        while true do        
            self:setPos(x0 + levelWidth * sx * math.sin(t), self:getY() + sy * (1+math.cos(t*2)))
            t = t + 1
            yield()
        end

        self:destroy()
    end
    
    return e
end

local function shootingStar(x, y)
    local e = BaseEnemy.new{hp=25}
    e:setPos(x, y)
    e:setSpeed(2)
    e:setSpeedInc(-.025)
    
    e.update = function(self)
        yield(math.abs(self:getSpeed() / self:getSpeedInc()))
        self:setSpeedInc(.05)
    end
    
    e.animate = function(self)
        while true do
            self:setDrawAngle(self:getDrawAngle() + 3)
            yield()
        end
    end
    
    return e
end

local function shootingStars(x0, x1, n)
    local dx = dx or 0

    local e = {}
    for i=1,n do
        local x = x0 + (x1-x0) * (i-.5) / n
        local y = -16
        e[i] = shootingStar(x, y)
    end
    return e
end

local function midBoss()
    local e = BaseEnemy.new{hp=1250}
    e:setPos(levelWidth/2, -32)
    e:setDrawAngleAuto(true)
    e:setAngleInc(5)
    
    e.fire = function(self, a)
        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setAngle(a)
        return s
    end
    
    e.fire2 = function(self, a)
        local s = self:fire(a)
        s:setSpeed(1.0 + 1.0 * math.random())
        return s
    end
    
    e.fire3 = function(self, a)
        local s = self:fire(a)
        s.animate = function(self)
            while true do
                local f = math.cos(1 * self:getY())
                s:setColor(0, .5 + .5 * f, .75 + .25 * f, 1)
                yield()
            end
        end
        s:setColNode(0, enemyShotColType, CircleColNode.new(12.0))
        s:setScale(1, 1)
        s:setZ(s:getZ() - 1)
        return s
    end
    
    e.fireThread = function(self)
        while self.hp > 1000 do
            local a = self:getAngle()
            self:fire(a)
            self:fire(a+128)
            self:fire(a+256)
            self:fire(a+384)
            yield(3)
        end
        
        while self.hp > 500 do
            for i=1,30 do
                local a = self:getAngle()
                self:fire2(a)
                self:fire2(a+256)
                yield(1)
            end
            
            for i=1,30 do
                local a = self:getAngle()
                self:fire2(a+128)
                self:fire2(a+384)
                yield(1)
            end
        end
        
        yield(30)
        
        while true do            
            for i=1,9 do
                local a = self:getAngle()
                self:fire2(a + math.random(0, 128))
                self:fire2(a + math.random(128, 256))
                self:fire2(a + math.random(256, 384))
                self:fire2(a + math.random(384, 512))
                yield(2)
            end

            local a = self:getAngle()
            self:fire3(a)
            self:fire3(a + 128)
            self:fire3(a + 256)
            self:fire3(a + 384)
            yield(4)
        end
    end
    
    e.update = function(self)    
        local targetX = levelWidth*.5
        local targetY = levelHeight*.2
        while self:getY() < targetY-.5 do
            local dx = .1 * (targetX - self:getX())
            local dy = .1 * (targetY - self:getY())
            self:setPos(self:getX() + dx, self:getY() + dy)
            yield()
        end
        
        self:addThread(self.fireThread)

        while true do
        
            yield()
        end

        self:destroy()
    end
        
    return e
end

-------------------------------------------------------------------------------

local GeomEnemy = {
    points=nil
    }

function GeomEnemy.new(self)
	self = extend(GeomEnemy, self or {})
	self = BaseEnemy.new(self)        
        
    self.points = self.points or {}
    self:setPos(levelWidth/2, -32)
        
    return self
end

function GeomEnemy:update()
    local pts = self.points
    if #pts >= 2 then
        self:setPos(pts[1], pts[2])
    end
    
    self:addThread(self.fireThread)
    
    for i=3,#pts,2 do
        self:setSpeed(2)
        self:setSpeedInc(0.01)
        moveTo(self, pts[i], pts[i+1], 9999)        
        self:setSpeed(0)
    end
end

function GeomEnemy:fireThread()	
    local a = math.random(0, 512)
    local maxR = 120
    local halfR = maxR / 2
    while true do
        for r=1,maxR do
            local rr = r / halfR
            if r > halfR then
                rr = (maxR-r) / halfR
            end
            rr = 16 * rr
            
            if rr > 2 then
                self:fire(a, rr)
            end
            yield()
        end
        
        yield(10)
    end
end

function GeomEnemy:fire(a, r)
    local s = BaseShot.new(self)
    s:setColNode(0, enemyShotColType, CircleColNode.new(r))
            
    s:setTexture(texStore:get("test.png#laser"));
    s:setDrawAngleAuto(true)
    s:setBlendMode(BlendMode.ADD)
    
    s:setSpeed(1)
    s:setAngle(a)
    s:setScale(r / 16, r / 16)
end

local function geomPatterns()
    local lx = 32
    local cx = levelWidth/2
    local rx = levelWidth-32
    local by = levelHeight-32
    local ky = levelHeight + 128
    
    local gs = {}
    
    table.insert(gs, GeomEnemy.new{hp=500, points={cx,-32,cx,64,rx,64,rx,by,lx,by,lx,ky}})
    table.insert(gs, GeomEnemy.new{hp=500, points={lx,-32,lx,192,cx,192,cx,192,cx,ky}})
    table.insert(gs, GeomEnemy.new{hp=500, points={-32,64,rx,64,rx,192,lx,192,lx,256,cx,256,cx,ky}})
    
    return gs
end

-------------------------------------------------------------------------------

local SuicideEnemy = {
    hp=75
    }

function SuicideEnemy.new(self)
	self = extend(SuicideEnemy, self or {})
	self = BaseEnemy.new(self)        
        
    self:setPos(math.random(-16, levelWidth+16), -32)    
    self:setSpeed(2)
    self:setAngle(256)
    
    return self
end

function SuicideEnemy:update()
    local frames = 60
    while frames > 0 do
        local p = getClosestPlayer(self:getX(), self:getY())
        if p ~= nil then
            if moveTo(self, p:getX(), p:getY(), frames) then
                break
            end
        end
        frames = frames - 5
        yield()
    end
    self:addThread(self.suicide)
end

function SuicideEnemy:suicide()
    self:setSpeed(0)
    self:setColNode(0, nil)
    self:setVisible(false)
    for layer=1,3 do
        for a=4*layer,512,16 do
            self:fire(a)
        end
        yield(6)
    end
    self:destroy()
end

function SuicideEnemy:fire(a)
    local s = BaseShot.new(self)
    s:setColNode(0, enemyShotColType, CircleColNode.new(3))
    s:setScale(.5, .5)
    s:setAngle(a)
    return s
end

local function suicidePatterns()
    local gs = {}
    
    local e = SuicideEnemy.new()
    e:setPos(-16, -16)
    table.insert(gs, e)
    
    local e = SuicideEnemy.new()
    e:setPos(levelWidth+16, -16)
    table.insert(gs, e)
    
    local e = SuicideEnemy.new()
    e:setPos(-16, 64)
    table.insert(gs, e)
    
    local e = SuicideEnemy.new()
    e:setPos(levelWidth+16, 64)
    table.insert(gs, e)
    
    return gs
end

-------------------------------------------------------------------------------

local ReflectorEnemy = {
    hp=350
    }

function ReflectorEnemy.new(self)
	self = extend(ReflectorEnemy, self or {})
	self = BaseEnemy.new(self)        

    self:setColNode(1, enemyShotDetectorColType, CircleColNode.new(12))
    self:setPos(levelWidth/2, -32)    
    self:setSpeed(2)
    self:setAngle(256)
    self:setAngleInc(3)
    self:setDrawAngleAuto(true)
    self:setColor(0, 1, 0, 1)
    
    return self
end

function ReflectorEnemy:update()
    local ai = self:getAngleInc()
    self:setAngleInc(0)
    yield(64)
    self:setSpeed(0)
    self:setAngleInc(ai)
    yield(300)
    self:setAngleInc(-ai)
    yield(300)
    self:destroy()
end

function ReflectorEnemy:onCollision(other, myNode, otherNode)
    if myNode:getType() == enemyShotDetectorColType then
        if not other.reflectorIgnore then
            self:splinter(other)
        end
    else
        THSprite.onCollision(self, other, myNode, otherNode)
    end
end

function ReflectorEnemy:splinter(base)
    for i=1,base:getWidth()/2 do
        s = BaseShot.new(base)
        s.reflectorIgnore = true
        s:setScale(base:getScaleX()*.50, base:getScaleY()*.50)
        s:setColNode(0, enemyShotColType, CircleColNode.new(s:getWidth()*.4))
        s:setAngle(self:getAngle() + 128 * math.random(0, 4))
        s:setAngleInc(self:getAngleInc() * .15)
        s:setSpeed(base:getSpeed() * .5)
    end
    base:destroy()
end

local ReflectorFeeder = {
    hp=200,
    target=nil
    }

function ReflectorFeeder.new(target, self)
	self = extend(ReflectorFeeder, self or {})
	self = BaseEnemy.new(self)        

    self.target = target
    
    self:setDrawAngle(64)
    self:setColor(1, 0, 0, 1)
    self:setSpeed(1)
    
    return self
end

function ReflectorFeeder:update()
    local target = self.target
    local tx = target:getX()
    local ty = target:getY()
    while true do
        if not target:isDestroyed() then
            tx = target:getX()
            ty = target:getY()
        end
        
        self:fire(tx, ty)
        yield(4)
    end
end

function ReflectorFeeder:fire(x, y)
    local s = BaseShot.new(self)
    s:setTexture(texStore:get(texpfx .. "shotBlue"))
    s:setAngle(math.atan2(y-self:getY(), x-self:getX()))
    s:setSpeed(5)
    return s
end
    
local function reflectorPatterns()
    local gs = {}
    
    local reflector = ReflectorEnemy.new()
    table.insert(gs, reflector)
    
    local e = ReflectorFeeder.new(reflector)
    e:setPos(32, -16)
    table.insert(gs, e)
    
    yield(90)
    
    local e = ReflectorFeeder.new(reflector)
    e:setPos(levelWidth-32, -16)
    table.insert(gs, e)
    
    return gs
end

-------------------------------------------------------------------------------

local function spellcard1(boss)
    boss:reset()
    
    local fire = function(self, a)
        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setAngle(a)
        s:setSpeed(1.5)
        
        local ai = .5 + .5 * math.random()
        if a > 256 then
            ai = -ai
        end
        s:setAngleInc(ai)
        
        s.update = function(self)
            yield(90)
            self:setTexture(texStore:get(texpfx .. "shotBlue"))
            self:setAngleInc(0)
        end
        
        return s
    end
    
    boss:addThread(function(self)
        while true do
            fire(self, 128 + math.random(0, 4) * 64)
            fire(self, 384 + math.random(1, 3) * 64)
            yield(2)
        end
    end)
    
    boss:setSpeed(1.5)
    while true do
        moveTo(boss, 96, boss:getY(), 999)
        yield(30)
        moveTo(boss, levelWidth-96, boss:getY(), 999)
        yield(30)
    end
end

local function spellcard2(boss)
    boss:reset()
    boss.invincible = true
    while boss:getScaleX() > .75 or boss:getScaleY() > .75 do
        boss:setScale(boss:getScaleX()*.99, boss:getScaleY()*.99)
        yield()
    end
    boss:setColNode(0, enemyColType, RectColNode.new(-22, -22, 44, 44))
    boss.invincible = false
    
    local fire = function(self, a)
        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setAngle(a)
        s:setSpeed(1.5)
        return s
    end
    
    local explodeCrap = function(self)
        for a=0, 512, math.random(8, 24) do
            local s = BaseShot.new(self)
            s:setAngle(a)
            s:setSpeed(1.5)
            s:setTexture(texStore:get(texpfx .. "shotBlue"))
        end
    end
    
    boss:addThread(function(self)
        while true do
            fire(self, self:getAngle() + 256 + math.random(-64, 64))
            fire(self, self:getAngle() + 256 + math.random(-64, 64))
            fire(self, self:getAngle() + 256 + math.random(-64, 64))
            yield(2)
        end
    end)
    
    local spd = 1.5
    local spdi = 0.05
    while true do
        boss:setSpeed(spd)
        boss:setSpeedInc(spdi)
        moveTo(boss, 24, 24, 999)
        boss:setSpeed(0)
        boss:setSpeedInc(0)
        explodeCrap(boss)
        yield(30)
        
        boss:setSpeed(spd)
        boss:setSpeedInc(spdi)
        moveTo(boss, 24, levelHeight-24, 999)
        boss:setSpeed(0)
        boss:setSpeedInc(0)
        explodeCrap(boss)
        yield(30)
        
        boss:setSpeed(spd)
        boss:setSpeedInc(spdi)
        moveTo(boss, levelWidth-24, levelHeight-24, 999)
        boss:setSpeed(0)
        boss:setSpeedInc(0)
        explodeCrap(boss)
        yield(30)
        
        boss:setSpeed(spd)
        boss:setSpeedInc(spdi)
        moveTo(boss, levelWidth-24, 24, 999)
        boss:setSpeed(0)
        boss:setSpeedInc(0)
        explodeCrap(boss)
        yield(30)
    end
end

local function spellcard3(boss)
    boss:reset()

    local fire = function(self, a, ai)
        local s = BaseShot.new(self)
        s:setColNode(0, enemyShotColType, CircleColNode.new(3))
        s:setScale(.5, .5)
        s:setSpeed(4)
        s:setAngle(a)
        s:setAngleInc(ai)
        s.update = function(self)
            yield(120)
            self:setAngleInc(0)
        end
        return s
    end

    local fire2 = function(self, a, frames)
        local s = BaseShot.new(self)
        s:setTexture(texStore:get(texpfx .. "shotBlue"))
        s:setSpeed(2.5)
        s:setAngle(a)
        s.update = function(self)
            yield(frames)
            local spd = self:getSpeed()
            self:setSpeed(0)
            yield(300 - frames)
            self:setSpeed(spd)
            self:setAngle(256)
        end
        return s
    end
    
    while true do
        boss:setSpeed(1)
        moveTo(boss, math.random(32, levelWidth-32), math.random(32, 64), 90)
        boss:setSpeed(0)
        
        for frame=1,90 do
            for a=64,256,24 do
                fire(boss, a, 1)
            end
            for a=256,448,24 do
                fire(boss, a, -1)
            end
            yield(2)
        end
        
        for i=1,60 do
            local frames = i
            local dir = 128
            if i > 30 then
                frames = frames - 30
                dir = 384
            end
            fire2(boss, dir, 16 + 12 * frames)
        end
    end
end

local SquareBoss = {
    }

function SquareBoss.new(self)
    self = extend(SquareBoss, self or {})
    self = THBoss.new(self)
    
    self:setTexture(texStore:get(texpfx .. "boss0"))
    self:setColNode(0, enemyColType, RectColNode.new(-30, -30, 62, 62))
    self:setAngle(256)
    self:setDrawAngleAuto(false)
    self:setDrawAngle(0)
    
    self:addSpellcard(Spellcard.new{time=45, hp=1000, update=spellcard1})
    self:addSpellcard(Spellcard.new{time=60, hp=500, update=spellcard2})
    self:addSpellcard(Spellcard.new{time=60, hp=1500, update=spellcard3})
    
    return self
end
    
function SquareBoss:reset()
    self.invincible = true
    self:setColor(1, 1, 1, 1)
    self:setSpeed(1)
    self:setSpeedInc(0.05)
    moveTo(self, levelWidth/2, 64, 999)
    self:setSpeed(0)
    self:setSpeedInc(0)
    self.invincible = false
end
    
local function createBoss()
    local es = {}
    table.insert(es, SquareBoss.new())
    return es
end

-------------------------------------------------------------------------------

function teststage1()
	scrollingBackground(texStore:get("bgscroll.png"), .01, 2)

    --Rotating emitters
    local e = enemy0()
    waitForDeath(e)

    local e1 = enemy0(levelWidth*.33, -16, 0, .30)
    yield(60)
    local e2 = enemy0(levelWidth*.67, -16, 64, .30)
    waitForDeath(e1, e2)
    yield(60)
    
    --Falling lines
    local e = {}
    append(e, shootingStars(0, levelWidth, 10))
    yield(180)
    append(e, shootingStars(-levelWidth/20, levelWidth+levelWidth/20, 10))
    yield(120)
    append(e, shootingStars(0, levelWidth, 10))
    yield(60)
    append(e, shootingStars(-levelWidth/20, levelWidth+levelWidth/20, 10))
    yield(30)
    append(e, shootingStars(0, levelWidth*.33, 10))
    append(e, shootingStars(levelWidth*.67, levelWidth, 10))
    yield(30)
    append(e, shootingStars(levelWidth*.33, levelWidth*.67, 10))
    yield(30)
    append(e, shootingStars(0, levelWidth*.33, 10))
    append(e, shootingStars(levelWidth*.67, levelWidth, 10))
    yield(30)
    append(e, shootingStars(levelWidth*.33, levelWidth*.67, 10))
    waitForDeath(e)
    yield(60)

    --Midboss time
    local e = {}
    table.insert(e, midBoss())
    waitForDeath(e)
    yield(180);
        
    local e = {}
    append(e, geomPatterns())
    yield(300)
    table.insert(e, SuicideEnemy.new())
    waitForDeath(e)
    
    waitForDeath(suicidePatterns())
    yield(120)
    
    waitForDeath(reflectorPatterns())
    yield(120)
    
    waitForDeath(createBoss())    
    
    local td = TextDrawable.new(overlayField)
	td:setPos(screenWidth/2, screenHeight/2)
	td:setZ(-100)
	td:setBlockAnchor(5)
	td:setFontName("DejaVuSans") --fontname is the file name without extension
	td:setFontStyle(FontStyle.BOLD)
	td:setFontSize(24)
	td:setOutlineColor(.1, .1, .1)
	td:setOutlineSize(4)
	td:setText("Game Over")
    
end
