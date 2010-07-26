
function main()	
	--returnTitle0()
	restart0()
end

function returnTitle()
	globalReset("returnTitle0")
end

function returnTitle0()
	initFields()
	mainMenu()
	start()
end

function restart()
	globalReset("restart0")
end

function restart0()
	initFields()
	start()
end

function initFields()
	levelWidth = 384
	levelHeight = 448

	--Setup collision matrix
	local colMatrix = ColMatrix.new()
	playerColType = colMatrix:newColType()
	playerGrazeColType = colMatrix:newColType()	
	playerItemColType = colMatrix:newColType()	
	playerItemMagnetColType = colMatrix:newColType()	
	playerShotColType = colMatrix:newColType()
	itemColType = colMatrix:newColType()	
	enemyColType = colMatrix:newColType()	
	enemyShotColType = colMatrix:newColType()	
	colMatrix:setColliding(playerColType, enemyColType)
	colMatrix:setColliding(playerColType, enemyShotColType)
	colMatrix:setColliding(playerGrazeColType, enemyColType)
	colMatrix:setColliding(playerGrazeColType, enemyShotColType)
	colMatrix:setColliding(itemColType, playerItemColType)
	colMatrix:setColliding(playerItemMagnetColType, itemColType)
	colMatrix:setColliding2(playerShotColType, enemyColType)

	--Create the background area (id=0)
	backgroundField = Field.new(0, 0, 0, screenWidth, screenHeight, 0)

	--Create the game area (id=1)
	gameField = Field.new(1,
		(screenWidth-levelWidth)/2, (screenHeight-levelHeight)/2,
		levelWidth, levelHeight, 32)
	
	gameColField = gameField:getColField()	
	gameColField:setColMatrix(colMatrix)
	
	--Create the overlay area (id=2)
	overlayField = Field.new(2, 0, 0, screenWidth, screenHeight, 0)	
end

function start()
	--soundEngine:setBGM("bgm/bgm01.ogg");

	setBackground("screen-border.png", 30)
	buildLevel()

	Thread.new(pauseListener)

	Thread.new(function()
		while true do
			if input:consumeKey(Keys.Q) then
				local objects = gameField:getAllObjects()
				for n=1,objects.length do
					--Do something interesting
				end
			elseif input:consumeKey(Keys.B) then
				Thread.new(createBoss01)
			elseif input:consumeKey(Keys.D) then
				Thread.new(dialogTest)
			elseif #players < 2 and input:consumeKey(Keys.L) then
				Thread.new(fireLaser)
			end
			yield()
		end
	end)
	
	Thread.new(stressTest, 50, 200)
	
	--Thread.new(stressTest, 25, 20)		
end

function dialogTest()
	local p = "player-dialog.png"

	local d = Dialog.new()
	d:addPage("[Player]\nHerp.\nDerp.", p, nil)
	d:addPage("TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT TEXT", p, p)
	
	while not d:isDestroyed() do
		yield()
	end
end

function createBoss01()
	local spellcard = Spellcard.new{time=30, hp=100}
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

function stressTest(a, b)
	for group=1,a do
		for n=1,b do
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
