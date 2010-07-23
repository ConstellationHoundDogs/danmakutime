
Invulnerable = {
	xmul=100,
	ymul=100
	}

function Invulnerable.new(self)
	self = extend(Invulnerable, self or {})
	return THSprite.new(self)
end

function Invulnerable:init()
	self:setPos(400, 300);
	self:setZ(-100)
	self:setTexture(texStore:get("test.png#g1"));
	self:setColNode(0, enemyColType, CircleColNode.new(7))
end

function Invulnerable:update()
	local n = 0
	while n < 100 do	
		self:setPos(levelWidth/2 + self.xmul * math.cos(n),
			levelHeight/2 + self.ymul * math.sin(n))
		n = n + 1
		
		yield()
	end
	
	self:destroy()
end

function Invulnerable:animate()
	while false do
		self:setTexture(texStore:get("test.png#g0"));
		yield(10)
		self:setTexture(texStore:get("test.png#g1"));
		yield(10)
	end
end

function Invulnerable:onDestroy()
	--return false from this function to prevent the destruction of the object
	--You can call destroy() again later to attempt another destruction
	return false
end

--------------------------------------------------------------------------------

function main()
	levelWidth = 384
	levelHeight = 448
	
	--Create the overlay field (id=999)
	overlayField = Field.new(999, 0, 0, screenWidth, screenHeight, 0)

	--mainMenu()

	--soundEngine:setBGM("bgm/bgm01.ogg");

	setBackground("level-bg.png", 30)
	buildLevel()

	local ghost = Invulnerable.new{xmul=100, ymul=100}	
    yield(10)
	local ghost2 = Invulnerable.new{xmul=-100, ymul=100}
	
	Thread.new(function()
		while true do
			if input:consumeKey(Keys.Q) then
				print("The global key listener thread saw your key. And will proceed to kill you.")
				player:destroy()
			end
			yield()
		end
	end)
	
	--Thread.new(fireLaser)
	
	--Thread.new(tenK)
	
	local spellcard = Spellcard.new{time=100, hp=100}
	spellcard.update = function(self, boss)
		while true do
			boss:setPos(math.random(0, levelWidth), math.random(0, levelHeight/2))
			yield(30)
		end
	end
	
	local boss = THBoss.new()
	boss:setTexture(texStore:get("test.png#g0"))	
	boss:addSpellcard(spellcard)
end

function tenK()
	for group=1,50 do
		for n=1,200 do
			local s = THSprite.new{hp=1, power=1}
			s:setColNode(0, enemyColType, CircleColNode.new(7))
			s.dropItems = function(self)
				dropPointItems(self:getX(), self:getY(), math.random(0, 3), math.random(0, 1))
				dropPowerItems(self:getX(), self:getY(), math.random(0, 3), math.random(0, 1))
			end
			
			if math.random(10) >= 10 then
				s:setTexture(texStore:get("test.png#g0"));
				s:setZ(-1)
				s:setDrawAngleAuto(false)
			else
				s:setTexture(texStore:get("test.png#g1"));
				s:setBlendMode(BlendMode.ADD)
			end
			
			s:setPos(levelWidth*.9, levelHeight*.35)
			s:setSpeed(2 + math.random() * 2)
			s:setAngle(256)
			s:setAngleInc(2 + math.random() * 1)
			
			--soundEngine:playSound("sfx01.ogg")
		end		
		yield(10)
	end
end

function fireLaser()
	while true do
		local r = 8
	
		local s = THSprite.new{hp=1, power=1}
		--s:setColNode(0, enemyShotColType, LineSegColNode.new(0, -8+r, 0, 8-r, r))
		--lineseg will have length==0, which is the same as this circle col node:
		s:setColNode(0, enemyShotColType, CircleColNode.new(r))
				
		s:setTexture(texStore:get("test.png#laser"));
		s:setBlendMode(BlendMode.ADD)
		
		s:setPos(levelWidth*.9, -32)
		s:setSpeed(2)
		s:setAngle(256)
		s:setAngleInc(.5)
		
		yield(2)
	end
end
